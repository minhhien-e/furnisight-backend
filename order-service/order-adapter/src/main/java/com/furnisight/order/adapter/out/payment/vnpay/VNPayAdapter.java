package com.furnisight.order.adapter.out.payment.vnpay;

import com.furnisight.order.application.payment.port.out.PaymentGatewayPort;
import com.furnisight.order.domain.entities.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class VNPayAdapter implements PaymentGatewayPort {

    private final VNPayConfig vnPayConfig;

    @Override
    public String getPaymentMethod() {
        return "vnpay";
    }

    @Override
    public String generatePaymentUrl(Order order, String clientIp) {
        long amount = Math.round(order.getTotalAmount() * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        String txnRef = order.getOrderCode() + "_" + System.currentTimeMillis();
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + order.getOrderCode());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", clientIp != null && !clientIp.isEmpty() ? clientIp : "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            return vnPayConfig.getUrl() + "?" + queryUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate VNPay URL", e);
        }
    }

    @Override
    public com.furnisight.order.application.payment.port.out.PaymentCallbackResult processCallback(Map<String, String> callbackParams) {
        if (!verifyCallback(callbackParams)) {
            return com.furnisight.order.application.payment.port.out.PaymentCallbackResult.builder()
                    .success(false)
                    .errorMessage("Invalid signature")
                    .build();
        }

        String txnRef = callbackParams.get("vnp_TxnRef");
        String orderCode = txnRef;
        if (orderCode != null && orderCode.contains("_")) {
            orderCode = orderCode.substring(0, orderCode.lastIndexOf('_'));
        }
        String responseCode = callbackParams.get("vnp_ResponseCode");
        String transactionStatus = callbackParams.get("vnp_TransactionStatus");
        String amountStr = callbackParams.get("vnp_Amount");
        String payDateStr = callbackParams.get("vnp_PayDate");

        boolean success = "00".equals(responseCode) && "00".equals(transactionStatus);
        Double amount = null;
        if (amountStr != null) {
            amount = Double.parseDouble(amountStr) / 100.0;
        }

        java.time.LocalDateTime paidAt = null;
        if (payDateStr != null && payDateStr.length() == 14) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            paidAt = java.time.LocalDateTime.parse(payDateStr, formatter);
        } else {
            paidAt = java.time.LocalDateTime.now();
        }

        return com.furnisight.order.application.payment.port.out.PaymentCallbackResult.builder()
                .success(success)
                .orderCode(orderCode)
                .amount(amount)
                .paidAt(paidAt)
                .errorMessage(success ? null : "Payment failed with response code: " + responseCode)
                .build();
    }

    private boolean verifyCallback(Map<String, String> callbackParams) {
        String secureHash = callbackParams.get("vnp_SecureHash");
        Map<String, String> signParams = new HashMap<>(callbackParams);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(signParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = signParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            String computedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            return computedHash.equals(secureHash);
        } catch (Exception e) {
            return false;
        }
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * result.length);
        for (byte b : result) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
