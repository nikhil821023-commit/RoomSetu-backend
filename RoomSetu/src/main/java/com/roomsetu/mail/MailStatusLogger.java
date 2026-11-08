package com.roomsetu.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MailStatusLogger {

    @Value("${roomsetu.mail.provider:smtp}")
    private String provider;

    @Value("${roomsetu.mail.enabled}")
    private boolean enabled;

    @Value("${roomsetu.mail.from:}")
    private String from;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.username:}")
    private String smtpUser;

    @Value("${roomsetu.mail.brevo.api-key:}")
    private String brevoKey;

    @Value("${roomsetu.otp.console-fallback:true}")
    private boolean consoleFallback;

    private final EmailSender sender;

    public MailStatusLogger(EmailSender sender) {
        this.sender = sender;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStatus() {
        System.out.printf("[MAIL] sender=%s provider=%s enabled=%s fromSet=%s smtpHostSet=%s smtpUserSet=%s brevoKeySet=%s consoleFallback=%s%n",
                sender.getClass().getSimpleName(), provider, enabled, !from.isBlank(),
                !smtpHost.isBlank(), !smtpUser.isBlank(), !brevoKey.isBlank(), consoleFallback);
    }
}