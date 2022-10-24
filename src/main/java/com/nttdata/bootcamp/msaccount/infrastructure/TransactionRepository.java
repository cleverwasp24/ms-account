package com.nttdata.bootcamp.msaccount.infrastructure;

import com.nttdata.bootcamp.msaccount.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, Integer> {

    Flux<Transaction> findAllByAccountId(Integer accountId);
    Flux<Transaction> findAllByAccountIdAndTransactionDateBetween(Integer accountId, LocalDateTime start, LocalDateTime end);

}
