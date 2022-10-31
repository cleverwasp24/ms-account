package com.nttdata.bootcamp.msaccount.infrastructure;

import com.nttdata.bootcamp.msaccount.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, Long> {

    Flux<Transaction> findAllByAccountId(Long accountId);
    Flux<Transaction> findAllByAccountIdAndTransactionDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);

}
