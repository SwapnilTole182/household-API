package com.household.household.service;

import com.household.household.entity.AcData;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class DuplicateDetectionService {

    //Find duplicate ac data method
    public String generateRowHash(AcData data) {
        if (data == null) {
            return null;
        }

        // Using a distinct delimiter to prevent concatenation collisions
        String signature = String.join("|",
                String.valueOf(data.getYear()),
                data.getBrand() != null ? data.getBrand().name() : "",
                data.getModelName() != null ? data.getModelName() : "",
                data.getAcType() != null ? data.getAcType().name() : "",
                data.getCapacityInTon() != null ? data.getCapacityInTon().toPlainString() : "",
                data.getInverterNonInverter() != null ? data.getInverterNonInverter().name() : "",
                String.valueOf(data.getStarRating()),
                data.getLaunchingPrice() != null ? data.getLaunchingPrice().toPlainString() : ""
        );

        return generateSHA256(signature);
    }


    //Find duplicate washing machine data method
    public String generateRowHash(com.household.household.entity.WashingMachineData data) {
        if (data == null) {
            return null;
        }

        String signature = String.join("|",
                String.valueOf(data.getYear()),
                data.getBrand() != null ? data.getBrand().name() : "",
                data.getModelNumber() != null ? data.getModelNumber() : "",
                data.getWashingType() != null ? data.getWashingType().name() : "",
                data.getCapacityKg() != null ? data.getCapacityKg().toPlainString() : "",
                data.getLoadingType() != null ? data.getLoadingType().name() : "",
                data.getLaunchingPrice() != null ? data.getLaunchingPrice().toPlainString() : ""
        );

        return generateSHA256(signature);
    }

    private String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
