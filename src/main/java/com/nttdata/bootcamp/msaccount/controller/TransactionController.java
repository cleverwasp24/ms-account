package com.nttdata.bootcamp.msaccount.controller;

import com.nttdata.bootcamp.msaccount.dto.TransactionDTO;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import com.nttdata.bootcamp.msaccount.service.impl.TransactionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping(value = "/findAllTransactionsByClientId/{id}")
    @ResponseBody
    public Flux<Transaction> findAllTransactionsByClientId(@PathVariable Integer id) {
        return transactionService.findAllByAccountId(id);
    }

    @PostMapping(value = "/createTransaction")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.createTransaction(transactionDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<Transaction> findTransactionById(@PathVariable Integer id) {
        return transactionService.findById(id)
                .defaultIfEmpty(null);
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<Transaction> updateTransaction(@PathVariable Integer id, @RequestBody Transaction transaction) {
        return transactionService.update(id, transaction)
                .defaultIfEmpty(null);
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdTransaction(@PathVariable Integer id) {
        return transactionService.delete(id)
                .defaultIfEmpty(null);
    }
}
