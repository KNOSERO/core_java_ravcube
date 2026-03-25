package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.inteface.AbstractPublisher;
import com.ravcube.lib.event.DomainEvent;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

abstract class AbstractRollbackPublisher<E extends DomainEvent>
    extends SpringPublisher<E> implements AbstractPublisher<E> {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected final void onTransactionAfterRollback(E event) {
        on(event);
    }

    protected abstract void on(E event);
}
