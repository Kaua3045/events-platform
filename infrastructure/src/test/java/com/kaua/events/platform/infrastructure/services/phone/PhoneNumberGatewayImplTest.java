package com.kaua.events.platform.infrastructure.services.phone;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.exceptions.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberGatewayImplTest extends UnitTest {

    private final PhoneNumberGatewayImpl gateway = new PhoneNumberGatewayImpl();

    @Test
    void givenValidPhone_whenNormalize_thenReturnE164() {
        final var aRaw = "(11) 98765-4321";
        final var aRegion = "BR";

        final var aResult = this.gateway.normalizeToE164(aRaw, aRegion);

        assertEquals("+5511987654321", aResult);
    }

    @Test
    void givenInvalidPhoneForRegion_whenNormalize_thenThrowDomainException() {
        final var aRaw = "999999999";
        final var aRegion = "BR";

        final var aException = assertThrows(
                DomainException.class,
                () -> this.gateway.normalizeToE164(aRaw, aRegion)
        );

        assertEquals(1, aException.getErrors().size());
        assertEquals("phoneNumber", aException.getErrors().get(0).property());
        assertTrue(aException.getErrors().get(0).message().contains("is not valid for region 'BR'"));
    }

    @Test
    void givenUnparsablePhone_whenNormalize_thenThrowDomainException() {
        final var aRaw = "+abc";
        final var aRegion = "BR";

        final var aException = assertThrows(
                DomainException.class,
                () -> this.gateway.normalizeToE164(aRaw, aRegion)
        );

        System.out.println(aException.getErrors().getFirst());

        assertEquals(1, aException.getErrors().size());
        assertEquals("phoneNumber", aException.getErrors().get(0).property());
        assertTrue(aException.getErrors().get(0).message().contains("could not be parsed"));
    }
}
