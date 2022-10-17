package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.CurrentAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.FixedTermDepositAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.SavingsAccountDTO;
import com.nttdata.bootcamp.msaccount.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Flux<Account> findAll();

    Mono<Account> create(Account account);

    Mono<Account> findById(Integer id);

    Mono<Account> update(Integer id, Account account);

    Mono<Void> delete(Integer id);

    Mono<SavingsAccountDTO> createSavingsAccount(SavingsAccountDTO accountDTO);

    Mono<CurrentAccountDTO> createCurrentAccount(CurrentAccountDTO accountDTO);

    Mono<FixedTermDepositAccountDTO> createFixedTermDepositAccount(FixedTermDepositAccountDTO accountDTO);

    Mono<Long> countClientAccounts(Integer clientId);

    Flux<Account> findAllByClientId(Integer id);


}
