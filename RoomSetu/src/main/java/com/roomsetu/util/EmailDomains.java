package com.roomsetu.util;

import com.roomsetu.entity.User;

import java.util.Set;

public final class EmailDomains {

    // Personal providers. Can't be used for the work/college badge.
    private static final Set<String> FREE_PROVIDERS = Set.of(
            "gmail.com", "googlemail.com", "yahoo.com", "yahoo.in", "yahoo.co.in", "ymail.com",
            "outlook.com", "hotmail.com", "live.com", "msn.com", "rediffmail.com",
            "icloud.com", "me.com", "proton.me", "protonmail.com", "aol.com",
            "zoho.com", "gmx.com", "mail.com", "yandex.com"
    );

    private static final Set<String> GMAIL = Set.of("gmail.com", "googlemail.com");

    // Providers where name+anything@domain is just another address for the same inbox.
    private static final Set<String> PLUS_ALIAS = Set.of(
            "outlook.com", "hotmail.com", "live.com", "icloud.com", "me.com", "proton.me", "protonmail.com"
    );

    // Starter list of throwaway-inbox services. Extend as you see abuse.
    private static final Set<String> DISPOSABLE = Set.of(
            "mailinator.com", "guerrillamail.com", "guerrillamail.net", "10minutemail.com",
            "tempmail.com", "temp-mail.org", "yopmail.com", "trashmail.com", "sharklasers.com",
            "getnada.com", "dispostable.com", "maildrop.cc", "throwawaymail.com", "fakeinbox.com"
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

    public static boolean isDisposable(String domain) {
        return domain != null && DISPOSABLE.contains(domain);
    }

    /** The verified work/college domain to show as a badge, or null. */
    public static String verifiedDomain(User u) {
        return u.getWorkEmailVerifiedAt() == null ? null : domainOf(u.getWorkEmail());
    }

    /**
     * One inbox, one identity. Gmail ignores dots and +tags; a few other big providers
     * ignore +tags. Everything else is left alone, because on other domains a "+" can be
     * a genuinely different mailbox and merging those would let one person into another's account.
     */
    public static String canonical(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        int at = email.lastIndexOf('@');
        if (at < 1) return email;

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        if (GMAIL.contains(domain)) {
            int plus = local.indexOf('+');
            if (plus >= 0) local = local.substring(0, plus);
            local = local.replace(".", "");
            domain = "gmail.com";
        } else if (PLUS_ALIAS.contains(domain)) {
            int plus = local.indexOf('+');
            if (plus >= 0) local = local.substring(0, plus);
        }
        return local + "@" + domain;
    }
}