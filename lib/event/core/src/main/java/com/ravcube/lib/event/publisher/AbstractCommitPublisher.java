package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.inteface.AbstractPublisher;
import com.ravcube.lib.event.DomainEvent;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

abstract class AbstractCommitPublisher<E extends DomainEvent>
    extends SpringPublisher<E> implements AbstractPublisher<E> {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected final void onTransactionAfterCommit(E event) {
        on(event);
    }

    protected abstract void on(E event);
}
