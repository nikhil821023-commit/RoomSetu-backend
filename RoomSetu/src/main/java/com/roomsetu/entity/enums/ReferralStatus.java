package com.roomsetu.entity.enums;

public enum ReferralStatus {
    SIGNED_UP,      // referred user has an account but hasn't completed a match yet
    REWARDED        // their first accepted match happened — reward is granted here, not at signup
}