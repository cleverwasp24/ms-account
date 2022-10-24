package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.PeriodDTO;
import com.nttdata.bootcamp.msaccount.dto.TransactionDTO;
import com.nttdata.bootcamp.msaccount.dto.TransferDTO;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {

    Flux<Transaction> findAll();

    Mono<Transaction> create(Transaction transaction);

    Mono<Transaction> findById(Integer id);

    Mono<Transaction> update(Integer id, Transaction transaction);

    Mono<Void> delete(Integer id);

    Mono<String> withdraw(TransactionDTO transactionDTO);

    Mono<String> deposit(TransactionDTO transactionDTO);

    Mono<String> transferOwnAccount(TransferDTO transferDTO);

    Mono<String> transferThirdAccount(TransferDTO transferDTO);

    Flux<Transaction> findAllByAccountId(Integer accountId);

     Mono<Long> countTransactionsAccountMonth(Integer accountId, LocalDateTime date);

    Flux<Transaction> findTransactionsAccountMonth(Integer accountId, LocalDateTime date);

    Mono<String> checkFields(Transaction transaction);

    Mono<Double> getFeeInAPeriod(Integer accountId, PeriodDTO periodDTO);

}
