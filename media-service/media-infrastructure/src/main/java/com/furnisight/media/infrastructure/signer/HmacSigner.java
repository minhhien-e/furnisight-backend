package com.furnisight.media.infrastructure.signer;

import com.furnisight.media.core.exception.InvalidSignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
@Component
public class HmacSigner {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final byte[] secretKeyBytes;

    public HmacSigner(SignerProperties props) {
        this.secretKeyBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Tạo chữ ký từ data
     */
    public String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGO));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(raw);
        } catch (Exception e) {
            throw new InvalidSignatureException("Failed to sign data", Map.of());
        }
    }

    /**
     * Verify chữ ký
     */
    public boolean verify(String data, String signature) {
        String expected = sign(data);
        // Constant-time compare chống timing attack
        return MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
