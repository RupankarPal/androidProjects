package com.example.bottonnavigation_and_recyclerview_implement_homepage.validation;

import android.content.Context;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.SettingEntity;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class otp_generater {
    private static final String SYSTEM_SECRET = "MY_SUPER_SECRET_KEY_12345";
    private static final long TIME_INTERVAL = 120; // 120 seconds (2 minutes)

    public static String otpgenerater(Context context) {
        SettingEntity dbhelper = new SettingEntity(context);
        String storedValue = dbhelper.getSecretValue(SYSTEM_SECRET);

        if (storedValue == null) {
            Random random = new Random();
            StringBuilder stringBuilder = new StringBuilder(16);
            stringBuilder.append(random.nextInt(9) + 1);
            for (int i = 0; i < 15; i++) {
                stringBuilder.append(random.nextInt(10));
            }
            storedValue = stringBuilder.toString();
            dbhelper.insertSecret(SYSTEM_SECRET, storedValue);
        }

        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long timeBlock = currentTimeSeconds / TIME_INTERVAL;

        return generateOtpForBlock(storedValue, timeBlock);
    }

    /**
     * Verifies the OTP by checking the current and previous time blocks (grace period).
     */
    public static boolean verifyOtp(Context context, String enteredOtp) {
        if (enteredOtp == null || enteredOtp.length() != 6) return false;

        SettingEntity dbhelper = new SettingEntity(context);
        String storedValue = dbhelper.getSecretValue(SYSTEM_SECRET);
        if (storedValue == null) return false;

        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long timeBlock = currentTimeSeconds / TIME_INTERVAL;

        // Check current block
        if (enteredOtp.equals(generateOtpForBlock(storedValue, timeBlock))) {
            return true;
        }

        // Check previous block (Grace period)
        if (enteredOtp.equals(generateOtpForBlock(storedValue, timeBlock - 1))) {
            return true;
        }

        return false;
    }

    private static String generateOtpForBlock(String secret, long timeBlock) {
        try {
            byte[] keyBytes = secret.getBytes();
            byte[] dataData = ByteBuffer.allocate(8).putLong(timeBlock).array();

            SecretKeySpec signKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(dataData);

            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

            int otp = binary % 1000000;
            return String.format("%06d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return "000000";
        }
    }
}
