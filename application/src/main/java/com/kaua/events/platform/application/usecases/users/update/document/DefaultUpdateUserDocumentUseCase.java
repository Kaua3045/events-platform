package com.kaua.events.platform.application.usecases.users.update.document;

import com.kaua.events.platform.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.events.platform.application.repositories.UserRepository;
import com.kaua.events.platform.application.wrapper.TracerWrapper;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.person.DocumentFactory;
import com.kaua.events.platform.domain.users.User;

import java.util.Objects;

public class DefaultUpdateUserDocumentUseCase extends UpdateUserDocumentUseCase {

    private final UserRepository userRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultUpdateUserDocumentUseCase(
            final UserRepository userRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public UpdateUserDocumentOutput execute(final UpdateUserDocumentInput input) {
        return this.tracerWrapper.traceWithReturn(
                "updateUserDocumentUseCase",
                ctx -> {
                    if (input == null) throw new UseCaseInputCannotBeNullException(UpdateUserDocumentUseCase.class);

                    final var aUser = ctx.runInSpan(
                            "user.retrieve.by-id",
                            () -> this.userRepository.userOfId(input.userId())
                                    .orElseThrow(NotFoundException.with(User.class, input.userId()))
                    );

                    final var aUserUpdated = aUser.updateDocument(DocumentFactory.create(
                            input.documentNumber(),
                            input.documentType()
                    ));

                    ctx.runInSpan("user.update", () -> this.userRepository.save(aUserUpdated));

                    return UpdateUserDocumentOutput.from(aUserUpdated);
                }
        );
    }
}
