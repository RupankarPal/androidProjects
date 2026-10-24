package com.example.bottonnavigation_and_recyclerview_implement_homepage.validation;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EmailHelper {

    private static final String API_URL = "https://stock-analysis-api-921311862826.asia-south1.run.app/otp";

    private final RequestQueue requestQueue;

    public EmailHelper(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public interface EmailCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }

    public void sendEmail(String email_address, String otp, EmailCallback callback) {
        try {
            if (email_address == null || email_address.trim().isEmpty()) {
                callback.onFailure(new Exception("Email address cannot be empty"));
                return;
            }

            String cleanEmail = email_address.trim();
            String cleanOtp = otp != null ? otp.trim() : "";

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", cleanEmail);
            jsonBody.put("otp", cleanOtp);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL,
                    response -> {
                        Log.d("EmailHelper", "Server Response: " + response);
                        String cleanResponse = response != null ? response.replace("\"", "").trim() : "OTP SEND SUCCESSFULLY";
                        callback.onSuccess(cleanResponse);
                    },
                    error -> {
                        String errorMessage = "Network Error";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String responseData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                Log.e("EmailHelper", "Error Data: " + responseData);
                                if (responseData.startsWith("{")) {
                                    JSONObject errorJson = new JSONObject(responseData);
                                    if (errorJson.has("detail")) {
                                        errorMessage = errorJson.getString("detail");
                                    } else if (errorJson.has("message")) {
                                        errorMessage = errorJson.getString("message");
                                    } else {
                                        errorMessage = responseData;
                                    }
                                } else {
                                    errorMessage = responseData.replace("\"", "").trim();
                                }
                            } catch (Exception ignored) {
                                errorMessage = "HTTP " + error.networkResponse.statusCode + " Error";
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = error.getMessage();
                        } else {
                            errorMessage = error.toString();
                        }
                        Log.e("EmailHelper", "Final Error Message: " + errorMessage);
                        callback.onFailure(new Exception(errorMessage));
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return requestBody.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("User-Agent", "Mozilla/5.0");
                    return headers;
                }
            };

            stringRequest.setShouldCache(false);
            // 30 seconds initial timeout with 0 retries to prevent duplicate attempts or premature timeout
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

}
