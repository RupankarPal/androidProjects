package com.example.bottonnavigation_and_recyclerview_implement_homepage.validation;

import android.os.Handler;
import android.os.Looper;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailHelper {

    private final String senderEmail = "@gmail.com"; // Replace with your Gmail
    private final String appPassword = ""; // Replace with 16-digit App Password
    // Callback interface to update the UI later
    public interface EmailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void sendEmail(String recipientEmail, String subject, String messageBody, EmailCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper()); // Used to push results back to main thread

        executor.execute(() -> {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "465");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.socketFactory.port", "465");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.debug", "true");

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, appPassword);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(messageBody);

                // Using explicit transport to catch connection/auth errors specifically
                Transport transport = session.getTransport("smtp");
                transport.connect("smtp.gmail.com", senderEmail, appPassword);
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();

                // Success: Notify main thread
                mainHandler.post(callback::onSuccess);

            } catch (MessagingException e) {
                e.printStackTrace();
                // Extract more descriptive error if possible
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("535")) {
                    errorMsg = "Authentication Failed (Check App Password): " + errorMsg;
                }
                final String finalError = errorMsg;
                // Failure: Notify main thread
                mainHandler.post(() -> callback.onFailure(new Exception(finalError)));
            }
        });
    }

}
