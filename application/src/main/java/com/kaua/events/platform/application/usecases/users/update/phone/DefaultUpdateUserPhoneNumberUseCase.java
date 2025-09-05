package com.kaua.events.platform.application.usecases.users.update.phone;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.gateways.PhoneNumberGateway;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.users.User;

import java.util.Objects;

public class DefaultUpdateUserPhoneNumberUseCase extends UpdateUserPhoneNumberUseCase {

    private final UserRepository userRepository;
    private final PhoneNumberGateway phoneNumberGateway;
    private final TracerWrapper tracerWrapper;

    public DefaultUpdateUserPhoneNumberUseCase(
            final UserRepository userRepository,
            final PhoneNumberGateway phoneNumberGateway,
            final TracerWrapper tracerWrapper
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.phoneNumberGateway = Objects.requireNonNull(phoneNumberGateway);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public UpdateUserPhoneNumberOutput execute(final UpdateUserPhoneNumberInput input) {
        return this.tracerWrapper.traceWithReturn(
                "updateUserDocumentUseCase",
                ctx -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(UpdateUserPhoneNumberUseCase.class);

                    final var aUser = ctx.runInSpan(
                            "user.retrieve.by-id",
                            () -> this.userRepository.userOfId(input.userId())
                                    .orElseThrow(NotFoundException.with(User.class, input.userId()))
                    );

                    final var aPhoneNumberE164 = this.phoneNumberGateway.normalizeToE164(input.phoneNumber(), input.defaultRegion());

                    final var aUserUpdated = aUser.updatePhoneNumber(aPhoneNumberE164);

                    ctx.runInSpan("user.update", () -> this.userRepository.save(aUserUpdated));

                    return UpdateUserPhoneNumberOutput.from(aUserUpdated);
                }
        );
    }
}
