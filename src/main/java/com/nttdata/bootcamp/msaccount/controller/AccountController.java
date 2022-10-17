package com.nttdata.bootcamp.msaccount.controller;

import com.nttdata.bootcamp.msaccount.dto.CurrentAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.FixedTermDepositAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.SavingsAccountDTO;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.service.impl.AccountServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/bootcamp/account")
public class AccountController {

    @Autowired
    AccountServiceImpl accountService;

    @GetMapping(value = "/findAllAccounts")
    @ResponseBody
    public Flux<Account> findAllAccounts() {
        return accountService.findAll();
    }

    @PostMapping(value = "/createSavingsAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SavingsAccountDTO> createSavingsAccount(@RequestBody SavingsAccountDTO savingsAccountDTO) {
        return accountService.createSavingsAccount(savingsAccountDTO);
    }

    @PostMapping(value = "/createCurrentAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CurrentAccountDTO> createCurrentAccount(@RequestBody CurrentAccountDTO currentAccountDTO) {
        return accountService.createCurrentAccount(currentAccountDTO);
    }

    @PostMapping(value = "/createFixedTermDepositAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FixedTermDepositAccountDTO> createFixedTermDepositAccount(@RequestBody FixedTermDepositAccountDTO fixedTermDepositAccountDTO) {
        return accountService.createFixedTermDepositAccount(fixedTermDepositAccountDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<Account> findAccountById(@PathVariable Integer id) {
        return accountService.findById(id)
                .defaultIfEmpty(null);
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<Account> updateAccount(@PathVariable Integer id, @RequestBody Account account) {
        return accountService.update(id, account)
                .defaultIfEmpty(null);
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdAccount(@PathVariable Integer id) {
        return accountService.delete(id)
                .defaultIfEmpty(null);
    }

}
