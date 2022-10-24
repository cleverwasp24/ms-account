package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<Account> findAll();

    Mono<Account> create(Account account);

    Mono<Account> findById(Integer id);

    Mono<Account> update(Integer id, Account account);

    Mono<Void> delete(Integer id);

    Mono<String> createSavingsAccount(SavingsAccountDTO accountDTO);

    Mono<String> createCurrentAccount(CurrentAccountDTO accountDTO);

    Mono<String> createFixedTermDepositAccount(FixedTermDepositAccountDTO accountDTO);

    Mono<String> createVIPAccount(VIPAccountDTO accountDTO);

    Mono<String> createPYMEAccount(PYMEAccountDTO accountDTO);

    Flux<Account> findAllByClientId(Integer id);

    Flux<Account> findAllByClientIdAndAccountType(Integer id, Integer type);

    Mono<String> checkFields(Account account);

}
