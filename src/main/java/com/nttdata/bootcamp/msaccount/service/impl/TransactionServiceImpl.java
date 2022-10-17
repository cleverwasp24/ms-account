package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.FixedTermDepositAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.TransactionDTO;
import com.nttdata.bootcamp.msaccount.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.msaccount.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    private TransactionDTOMapper transactionDTOMapper = new TransactionDTOMapper();

    @Override
    public Flux<Transaction> findAll() {
        log.info("Listing all transactions");
        return transactionRepository.findAll();
    }

    @Override
    public Mono<Transaction> create(Transaction transaction) {
        log.info("Creating transaction: " + transaction.toString());
        return transactionRepository.save(transaction);
    }

    @Override
    public Mono<Transaction> findById(Integer id) {
        log.info("Searching transaction by id: " + id);
        return transactionRepository.findById(id);
    }

    @Override
    public Mono<Transaction> update(Integer id, Transaction transaction) {
        log.info("Updating transaction with id: " + id + " with : " + transaction.toString());
        return transactionRepository.findById(id)
                .flatMap(savedTransaction -> {
                    savedTransaction.setId(transaction.getAccountId());
                    savedTransaction.setTransactionType(transaction.getTransactionType());
                    savedTransaction.setDescription(transaction.getDescription());
                    savedTransaction.setAmount(transaction.getAmount());
                    savedTransaction.setTransactionDate(transaction.getTransactionDate());
                    return transactionRepository.save(savedTransaction);
                });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        log.info("Deleting transaction with id: " + id);
        return transactionRepository.deleteById(id);
    }

    @Override
    public Mono<TransactionDTO> createTransaction(TransactionDTO transactionDTO) {
        log.info("Creating transaction: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO);
        return transactionRepository.save(transaction)
                .map(c -> transactionDTOMapper.convertToDto(c));
    }

}
