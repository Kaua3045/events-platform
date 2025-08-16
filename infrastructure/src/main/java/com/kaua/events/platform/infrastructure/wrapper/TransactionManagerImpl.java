package com.kaua.events.platform.infrastructure.wrapper;

import com.kaua.events.platform.application.wrapper.TransactionManager;
import com.kaua.events.platform.application.wrapper.TransactionResult;
import com.kaua.events.platform.domain.utils.Generated;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Generated
@Component
public class TransactionManagerImpl implements TransactionManager {

    private final TransactionTemplate transactionTemplate;

    public TransactionManagerImpl(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> TransactionResult<T> execute(Supplier<T> action) {
        try {
            T result = transactionTemplate.execute(status -> {
                try {
                    return action.get();
                } catch (final Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
            });
            return TransactionResult.success(result);
        } catch (Exception e) {
            return TransactionResult.failure((RuntimeException) e);
        }
    }
}
