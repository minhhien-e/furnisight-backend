package com.furnisight.media.infrastructure.antivirus;

import com.furnisight.media.core.antivirus.ScanResult;
import com.furnisight.media.core.antivirus.VirusScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClamAvVirusScanner implements VirusScanner {

    private static final int CHUNK_SIZE = 2048;
    private static final String ZINSTREAM = "zINSTREAM\0";
    private static final String OK_RESPONSE = "OK";
    private static final String FOUND_RESPONSE = "FOUND";
    private static final String ERROR_RESPONSE = "ERROR";

    private final ClamAvProperties properties;

    @Override
    public ScanResult scan(InputStream inputStream) {
        log.info("Starting ClamAV virus scan on host {}:{}", properties.getHost(), properties.getPort());
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(properties.getTimeoutMs());
            socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()), properties.getTimeoutMs());

            try (OutputStream out = new BufferedOutputStream(socket.getOutputStream());
                 InputStream in = socket.getInputStream()) {

                // Request zINSTREAM
                out.write(ZINSTREAM.getBytes(StandardCharsets.US_ASCII));
                out.flush();

                // Send chunks
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = inputStream.read(buffer)) >= 0) {
                    byte[] chunkSize = ByteBuffer.allocate(4).putInt(read).array();
                    out.write(chunkSize);
                    out.write(buffer, 0, read);
                }

                // Send 0-length chunk to denote end
                out.write(new byte[]{0, 0, 0, 0});
                out.flush();

                // Read response
                byte[] responseBuffer = new byte[CHUNK_SIZE];
                int responseLength = in.read(responseBuffer);
                if (responseLength <= 0) {
                    log.error("Empty response from ClamAV");
                    throw new RuntimeException("Empty response from ClamAV");
                }

                String response = new String(responseBuffer, 0, responseLength, StandardCharsets.US_ASCII).trim();
                log.info("ClamAV response: {}", response);

                if (response.endsWith(OK_RESPONSE)) {
                    return ScanResult.safe();
                } else if (response.contains(FOUND_RESPONSE)) {
                    log.warn("Virus found by ClamAV: {}", response);
                    return ScanResult.infected(parseVirusName(response));
                } else if (response.contains(ERROR_RESPONSE)) {
                    log.error("ClamAV scan error: {}", response);
                    throw new RuntimeException("ClamAV scan error: " + response);
                } else {
                    log.error("Unknown response from ClamAV: {}", response);
                    throw new RuntimeException("Unknown response from ClamAV: " + response);
                }
            }

        } catch (IOException e) {
            log.error("Failed to connect or communicate with ClamAV on {}:{}", properties.getHost(), properties.getPort(), e);
            throw new RuntimeException("ClamAV scan failed: " + e.getMessage(), e);
        }
    }

    private String parseVirusName(String response) {
        // ClamAV format: stream: Eicar-Test-Signature FOUND
        int streamIndex = response.indexOf("stream:");
        int foundIndex = response.indexOf("FOUND");

        if (streamIndex != -1 && foundIndex != -1 && (streamIndex + 7) <= foundIndex) {
            return response.substring(streamIndex + 7, foundIndex).trim();
        }

        // Fallback
        return response;
    }
}
