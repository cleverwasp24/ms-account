package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<Account> findAll();

    Mono<Account> create(Account account);

    Mono<Account> findById(Long id);

    Mono<Account> update(Long id, Account account);

    Mono<Void> delete(Long id);

    Mono<String> createSavingsAccount(SavingsAccountDTO accountDTO);

    Mono<String> createCurrentAccount(CurrentAccountDTO accountDTO);

    Mono<String> createFixedTermDepositAccount(FixedTermDepositAccountDTO accountDTO);

    Mono<String> createVIPAccount(VIPAccountDTO accountDTO);

    Mono<String> createPYMEAccount(PYMEAccountDTO accountDTO);

    Flux<Account> findAllByClientId(Long id);

    Flux<Account> findAllByClientIdAndAccountType(Long id, Integer type);

    Mono<String> checkFields(Account account);

}
