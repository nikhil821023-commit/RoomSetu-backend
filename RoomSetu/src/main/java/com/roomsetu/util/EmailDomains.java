package com.roomsetu.util;

import com.roomsetu.entity.User;

import java.util.Set;

public final class EmailDomains {

    // Starter list of personal providers. Extend as you see abuse.
    private static final Set<String> FREE_PROVIDERS = Set.of(
            "gmail.com", "googlemail.com", "yahoo.com", "yahoo.in", "yahoo.co.in", "ymail.com",
            "outlook.com", "hotmail.com", "live.com", "msn.com", "rediffmail.com",
            "icloud.com", "me.com", "proton.me", "protonmail.com", "aol.com",
            "zoho.com", "gmx.com", "mail.com", "yandex.com"
    );

    private EmailDomains() {}

    public static String domainOf(String email) {
        if (email == null) return null;
        int at = email.lastIndexOf('@');
        return at < 0 ? null : email.substring(at + 1).toLowerCase();
    }

    public static boolean isFreeProvider(String domain) {
        return domain != null && FREE_PROVIDERS.contains(domain);
    }

    /** The verified domain to show as a badge, or null if this user hasn't verified one. */
    public static String verifiedDomain(User u) {
        return u.getWorkEmailVerifiedAt() == null ? null : domainOf(u.getWorkEmail());
    }
}