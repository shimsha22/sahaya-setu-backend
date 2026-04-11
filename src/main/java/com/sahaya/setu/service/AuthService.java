package com.sahaya.setu.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateAndSendOtp(String identifier) {
        // Generate a random 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Save it in memory linked to their mobile or Aadhaar number
        otpStorage.put(identifier, otp);

        // SIMULATE SENDING THE SMS OR AADHAAR PING
        System.out.println("\n=======================================");
        System.out.println("MOCK SMS TRIGGERED");
        System.out.println("To Mobile/Aadhaar: " + identifier);
        System.out.println("Your Sahaya-Setu OTP is: " + otp);
        System.out.println("=======================================\n");

        return "OTP sent successfully to " + identifier;
    }

    public boolean verifyOtp(String identifier, String enteredOtp) {
        String storedOtp = otpStorage.get(identifier);

        // Check if the OTP exists and matches what the user typed
        if (storedOtp != null && storedOtp.equals(enteredOtp)) {
            otpStorage.remove(identifier); // Clean it up so it can't be reused!
            return true;
        }
        return false;
    }
}
