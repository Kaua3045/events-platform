package com.kaua.events.platform.application.gateways;

public interface PhoneNumberGateway {

    String normalizeToE164(String phoneNumber, String defaultRegion);
}
