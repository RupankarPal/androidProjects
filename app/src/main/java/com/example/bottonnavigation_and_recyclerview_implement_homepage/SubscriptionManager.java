package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.razorpay.Checkout;

import org.json.JSONObject;

public class SubscriptionManager {

    private static final String PREF_NAME = "SubscriptionPrefs";
    private static final String KEY_IS_PREMIUM = "isPremium";
    
    // RAZORPAY KEY - Replace with actual Key from Razorpay Dashboard
    public static final String RAZORPAY_KEY_ID = "";

    public static boolean isPremium(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_PREMIUM, false);
    }

    public static void setPremium(Context context, boolean status) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_PREMIUM, status).apply();
    }

    public static void startSubscriptionPayment(Activity activity) {
        if (activity == null) return;
        Checkout checkout = new Checkout();
        checkout.setKeyID(RAZORPAY_KEY_ID);
        
        try {
            JSONObject options = new JSONObject();
            options.put("name", "DeTrade Premium");
            options.put("description", "Premium Subscription (₹49/month)");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("amount", "4900"); // 49.00 INR in paise
            options.put("theme.color", "#9015C5");
            
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
