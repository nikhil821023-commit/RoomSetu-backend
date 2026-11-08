package com.roomsetu.sms;

/**
 * Same pattern as AiTextClient in Phase 6: an interface, one implementation for now,
 * and every caller treats delivery failure as an expected outcome to handle gracefully
 * — not an exception that should break the signup flow.
 */
public interface SmsGateway {
    boolean sendOtp(String phoneNumber, String otp);
}