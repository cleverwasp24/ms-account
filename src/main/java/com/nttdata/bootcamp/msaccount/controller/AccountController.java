package com.nttdata.bootcamp.msaccount.controller;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.service.impl.AccountServiceImpl;
import com.nttdata.bootcamp.msaccount.service.impl.TransactionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/bootcamp/account")
public class AccountController {

    @Autowired
    AccountServiceImpl accountService;

    @Autowired
    TransactionServiceImpl transactionService;

    @GetMapping(value = "/findAllAccounts")
    @ResponseBody
    public Flux<Account> findAllAccounts() {
        return accountService.findAll();
    }

    @GetMapping(value = "/findAllAccountsByClientId/{id}")
    @ResponseBody
    public Flux<Account> findAllAccountsByClientId(@PathVariable Long id) {
        return accountService.findAllByClientId(id);
    }

    @PostMapping(value = "/createSavingsAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createSavingsAccount(@RequestBody SavingsAccountDTO savingsAccountDTO) {
        return accountService.createSavingsAccount(savingsAccountDTO);
    }

    @PostMapping(value = "/createCurrentAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createCurrentAccount(@RequestBody CurrentAccountDTO currentAccountDTO) {
        return accountService.createCurrentAccount(currentAccountDTO);
    }

    @PostMapping(value = "/createFixedTermDepositAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createFixedTermDepositAccount(@RequestBody FixedTermDepositAccountDTO fixedTermDepositAccountDTO) {
        return accountService.createFixedTermDepositAccount(fixedTermDepositAccountDTO);
    }

    @PostMapping(value = "/createVIPAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createVIPAccount(@RequestBody VIPAccountDTO vipAccountDTO) {
        return accountService.createVIPAccount(vipAccountDTO);
    }

    @PostMapping(value = "/createPYMEAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createPYMEAccount(@RequestBody PYMEAccountDTO pymeAccountDTO) {
        return accountService.createPYMEAccount(pymeAccountDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Account>> findAccountById(@PathVariable Long id) {
        return accountService.findById(id)
                .map(account -> ResponseEntity.ok().body(account))
                .onErrorResume(e -> {
                    log.info("Account not found " + id, e);
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Account>> updateAccount(@PathVariable Long id, @RequestBody Account account) {
        return accountService.update(id, account)
                .map(a -> new ResponseEntity<>(a, HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdAccount(@PathVariable Long id) {
        return accountService.delete(id);
    }

    @GetMapping(value = "/findAllByClientId/{id}")
    @ResponseBody
    public Flux<Account> findAllByClientId(@PathVariable Long id) {
        return accountService.findAllByClientId(id);
    }

    @GetMapping(value = "/getFeeInAPeriod/{id}")
    @ResponseBody
    public Mono<Double> getFeeInAPeriod(@PathVariable Long id, @RequestBody PeriodDTO periodDTO) {
        return accountService.findById(id)
                .flatMap(account -> transactionService.getFeeInAPeriod(account.getId(), periodDTO))
                .switchIfEmpty(Mono.error(new Exception("Account not found")));
    }

}
