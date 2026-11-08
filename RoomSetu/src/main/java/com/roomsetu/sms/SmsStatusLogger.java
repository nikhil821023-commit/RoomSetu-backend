package com.roomsetu.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SmsStatusLogger {

    @Value("${roomsetu.sms.enabled}")
    private boolean enabled;

    @Value("${roomsetu.sms.provider:msg91}")
    private String provider;

    @Value("${roomsetu.sms.fast2sms.api-key:}")
    private String fast2smsKey;

    @Value("${roomsetu.otp.console-fallback:true}")
    private boolean consoleFallback;

    private final SmsGateway gateway;

    public SmsStatusLogger(SmsGateway gateway) {
        this.gateway = gateway;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStatus() {
        System.out.printf("[SMS] gateway=%s provider=%s enabled=%s fast2smsKeySet=%s consoleFallback=%s%n",
                gateway.getClass().getSimpleName(), provider, enabled, !fast2smsKey.isBlank(), consoleFallback);
    }
}