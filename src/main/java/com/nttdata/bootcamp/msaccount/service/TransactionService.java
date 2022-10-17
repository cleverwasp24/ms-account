package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.TransactionDTO;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Flux<Transaction> findAll();

    Mono<Transaction> create(Transaction transaction);

    Mono<Transaction> findById(Integer id);

    Mono<Transaction> update(Integer id, Transaction transaction);

    Mono<Void> delete(Integer id);

    public Mono<TransactionDTO> createTransaction(TransactionDTO accountDTO);

    Flux<Transaction> findAllByAccountId(Integer accountId);

}
