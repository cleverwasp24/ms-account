package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {

    Flux<Transaction> findAll();

    Mono<Transaction> create(Transaction transaction);

    Mono<Transaction> findById(Long id);

    Mono<Transaction> update(Long id, Transaction transaction);

    Mono<Void> delete(Long id);

    Mono<String> withdraw(TransactionDTO transactionDTO);

    Mono<String> deposit(TransactionDTO transactionDTO);

    Mono<String> transferOwnAccount(TransferDTO transferDTO);

    Mono<String> transferThirdAccount(TransferDTO transferDTO);

    Mono<String> cardPurchase(TransactionDTO transactionDTO);

    Mono<String> cardDeposit(TransactionDTO transactionDTO);

    Flux<Transaction> findAllByAccountId(Long accountId);

    Flux<Transaction> findAllByAccountIdDesc(Long accountId);

    Mono<Long> countTransactionsAccountMonth(Long accountId, LocalDateTime date);

    Flux<Transaction> findTransactionsAccountMonth(Long accountId, LocalDateTime date);

    Flux<Transaction> findTransactionsAccountPeriod(Long accountId, LocalDateTime start, LocalDateTime end);

    Mono<String> checkFields(Transaction transaction);

    Mono<CompleteReportDTO> generateCompleteReport(Long id, PeriodDTO periodDTO);

    Mono<Double> getFeeInAPeriod(Long accountId, PeriodDTO periodDTO);

    Mono<Transaction> findLastTransactionBefore(Long id, LocalDateTime date);

    Mono<AccountReportDTO> generateAccountReportCurrentMonth(Long id);

    Mono<AccountReportDTO> generateAccountReport(Long id, PeriodDTO periodDTO);
}
