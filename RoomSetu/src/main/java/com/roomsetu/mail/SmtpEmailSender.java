package com.roomsetu.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Used for every provider value except "brevo". */
@Component
@ConditionalOnExpression("!'${roomsetu.mail.provider:smtp}'.trim().equalsIgnoreCase('brevo')")
public class SmtpEmailSender implements EmailSender {

    @Value("${roomsetu.mail.enabled:false}")
    private boolean enabled;

    @Value("${roomsetu.mail.from:}")
    private String from;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public SmtpEmailSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public boolean send(String to, String subject, String body) {
        if (!enabled) {
            System.err.println("[Mail] Not sent: roomsetu.mail.enabled is false");
            return false;
        }
        if (from == null || from.isBlank()) {
            System.err.println("[Mail] Not sent: roomsetu.mail.from (MAIL_FROM) is empty");
            return false;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            System.err.println("[Mail] Not sent: no mail server configured (spring.mail.host is empty)");
            return false;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from.trim());
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("[Mail] Send failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return false;
        }
    }
}