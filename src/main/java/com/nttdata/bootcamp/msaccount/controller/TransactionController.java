package com.nttdata.bootcamp.msaccount.controller;

import com.nttdata.bootcamp.msaccount.dto.AccountReportDTO;
import com.nttdata.bootcamp.msaccount.dto.PeriodDTO;
import com.nttdata.bootcamp.msaccount.dto.TransactionDTO;
import com.nttdata.bootcamp.msaccount.dto.TransferDTO;
import com.nttdata.bootcamp.msaccount.model.Transaction;
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
@RequestMapping("/bootcamp/transaction")
public class TransactionController {

    @Autowired
    TransactionServiceImpl transactionService;

    @GetMapping(value = "/findAllTransactions")
    @ResponseBody
    public Flux<Transaction> findAllTransactions() {
        return transactionService.findAll();
    }


    @PostMapping(value = "/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> deposit(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.deposit(transactionDTO);
    }

    @PostMapping(value = "/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> withdraw(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.withdraw(transactionDTO);
    }

    @PostMapping(value = "/transferOwnAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> transferOwnAccount(@RequestBody TransferDTO transferDTO) {
        return transactionService.transferOwnAccount(transferDTO);
    }

    @PostMapping(value = "/transferThirdAccount")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> transferThirdAccount(@RequestBody TransferDTO transferDTO) {
        return transactionService.transferThirdAccount(transferDTO);
    }

    @PostMapping(value = "/cardDeposit")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> cardDeposit(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.cardDeposit(transactionDTO);
    }

    @PostMapping(value = "/cardPurchase")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> cardPurchase(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.cardPurchase(transactionDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Transaction>> findTransactionById(@PathVariable Long id) {
        return transactionService.findById(id)
                .map(transaction -> ResponseEntity.ok().body(transaction))
                .onErrorResume(e -> {
                    log.info("Transaction not found " + id, e);
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Transaction>> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        return transactionService.update(id, transaction)
                .map(t -> new ResponseEntity<>(t, HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdTransaction(@PathVariable Long id) {
        return transactionService.delete(id);
    }

    @GetMapping(value = "/findAllByAccountId/{id}")
    @ResponseBody
    public Flux<Transaction> findAllByAccountId(@PathVariable Long id) {
        return transactionService.findAllByAccountId(id);
    }

}
