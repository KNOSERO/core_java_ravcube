package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.routing.AbstractEventPublisher;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

abstract class AbstractRollbackPublisher<E extends DomainEvent>
    extends SpringPublisher<E> implements AbstractEventPublisher<E> {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected final void onTransactionAfterRollback(E event) {
        on(event);
    }

    protected abstract void on(E event);
}
