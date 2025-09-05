package com.kaua.events.platform.infrastructure.services.phone;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.kaua.events.platform.application.gateways.PhoneNumberGateway;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.validation.Error;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhoneNumberGatewayImpl implements PhoneNumberGateway {

    private final PhoneNumberUtil util = PhoneNumberUtil.getInstance();

    @Override
    public String normalizeToE164(final String phoneNumber, final String defaultRegion) {
        try {
            final var aParsed = this.util.parse(phoneNumber, defaultRegion);

            if (!this.util.isValidNumber(aParsed)) {
                throw DomainException.with(List.of(new Error(
                        "phoneNumber",
                        String.format("Phone number '%s' is not valid for region '%s'", phoneNumber, defaultRegion)
                )));
            }

            return this.util.format(aParsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw DomainException.with(List.of(new Error(
                    "phoneNumber",
                    String.format("Phone number '%s' could not be parsed", phoneNumber)
            )));
        }
    }
}
