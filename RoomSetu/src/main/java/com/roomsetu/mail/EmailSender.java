package com.roomsetu.mail;

public interface EmailSender {
    /** true if actually sent; false if disabled, misconfigured, or failed (caller falls back). */
    boolean send(String to, String subject, String body);
}