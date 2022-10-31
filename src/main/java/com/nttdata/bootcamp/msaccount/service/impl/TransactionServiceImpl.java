package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.PeriodDTO;
import com.nttdata.bootcamp.msaccount.dto.TransactionDTO;
import com.nttdata.bootcamp.msaccount.dto.TransferDTO;
import com.nttdata.bootcamp.msaccount.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.msaccount.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.model.enums.TransactionTypeEnum;
import com.nttdata.bootcamp.msaccount.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Log4j2
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountServiceImpl accountService;

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
    public Mono<Transaction> findById(Long id) {
        log.info("Searching transaction by id: " + id);
        return transactionRepository.findById(id);
    }

    @Override
    public Mono<Transaction> update(Long id, Transaction transaction) {
        log.info("Updating transaction with id: " + id + " with : " + transaction.toString());
        return transactionRepository.findById(id).flatMap(a -> {
            transaction.setId(id);
            return transactionRepository.save(transaction);
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting transaction with id: " + id);
        return transactionRepository.deleteById(id);
    }

    public Mono<String> deposit(TransactionDTO transactionDTO) {
        log.info("Making a deposit: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.DEPOSIT);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    return countTransactionsAccountMonth(transaction.getAccountId(), transaction.getTransactionDate()).flatMap(count -> {
                        if (a.getMaxFreeTransactions() <= count) {
                            //APLICAR COMISION POR DEPOSITO
                            transaction.calculateFee(AccountTypeEnum.valueOf(a.getAccountType()));
                        }
                        a.setBalance(a.getBalance() + transaction.getAmount() - transaction.getFee());
                        if (a.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Transaction fee couldn't be paid"));
                        }
                        transaction.setNewBalance(a.getBalance());
                        return accountService.update(a.getId(), a)
                                .flatMap(ac -> transactionRepository.save(transaction))
                                .flatMap(t -> Mono.just("Deposit done, new balance: " + a.getBalance()));
                    });
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    @Override
    public Mono<String> withdraw(TransactionDTO transactionDTO) {
        log.info("Making a withdraw: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.WITHDRAW);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    return countTransactionsAccountMonth(transaction.getAccountId(), transaction.getTransactionDate()).flatMap(count -> {
                        if (a.getMaxFreeTransactions() <= count) {
                            //APLICAR COMISION POR WITHDRAW
                            transaction.calculateFee(AccountTypeEnum.valueOf(a.getAccountType()));
                        }
                        a.setBalance(a.getBalance() - transaction.getAmount() - transaction.getFee());
                        if (a.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to withdraw"));
                        }
                        transaction.setNewBalance(a.getBalance());
                        return accountService.update(a.getId(), a)
                                .flatMap(ac -> transactionRepository.save(transaction))
                                .flatMap(t -> Mono.just("Withdraw done, new balance: " + a.getBalance()));
                    });
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    @Override
    public Mono<String> transferOwnAccount(TransferDTO transferDTO) {
        log.info("Own Account transfer: " + transferDTO.toString() + " destination account: " + transferDTO.getDestinationAccountId());
        Transaction transaction = transactionDTOMapper.convertToEntity(transferDTO, TransactionTypeEnum.TRANSFER_OWN);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(originAccount -> {
                    return accountService.findById(transaction.getDestinationAccountId()).flatMap(destinationAccount -> {
                        originAccount.setBalance(originAccount.getBalance() - transaction.getAmount());
                        if (originAccount.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to transfer"));
                        }
                        transaction.setNewBalance(originAccount.getBalance());
                        destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                        Transaction destinationAccountTransaction = transactionDTOMapper.generateDestinationAccountTransaction(transaction);
                        destinationAccountTransaction.setNewBalance(destinationAccount.getBalance());
                        return accountService.update(originAccount.getId(), originAccount)
                                .flatMap(oa -> transactionRepository.save(transaction))
                                .flatMap(ot -> accountService.update(destinationAccount.getId(), destinationAccount))
                                .flatMap(da -> transactionRepository.save(destinationAccountTransaction))
                                .flatMap(dt -> Mono.just("Transfer to own account done, new balance: " + originAccount.getBalance()));
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Destination account not found")));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Origin Account not found"))));
    }

    @Override
    public Mono<String> transferThirdAccount(TransferDTO transferDTO) {
        log.info("Third Account transfer: " + transferDTO.toString() + " destination account: " + transferDTO.getDestinationAccountId());
        Transaction transaction = transactionDTOMapper.convertToEntity(transferDTO, TransactionTypeEnum.TRANSFER_THIRD);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(originAccount -> {
                    return accountService.findById(transaction.getDestinationAccountId()).flatMap(destinationAccount -> {
                        originAccount.setBalance(originAccount.getBalance() - transaction.getAmount());
                        if (originAccount.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to transfer"));
                        }
                        transaction.setNewBalance(originAccount.getBalance());
                        destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                        Transaction destinationAccountTransaction = transactionDTOMapper.generateDestinationAccountTransaction(transaction);
                        destinationAccountTransaction.setNewBalance(destinationAccount.getBalance());
                        return accountService.update(originAccount.getId(), originAccount)
                                .flatMap(oa -> transactionRepository.save(transaction))
                                .flatMap(ot -> accountService.update(destinationAccount.getId(), destinationAccount))
                                .flatMap(da -> transactionRepository.save(destinationAccountTransaction))
                                .flatMap(dt -> Mono.just("Transfer to third account done, new balance: " + originAccount.getBalance()));
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Destination account not found")));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Origin Account not found"))));
    }

    @Override
    public Mono<String> cardDeposit(TransactionDTO transactionDTO) {
        log.info("Making a debit card deposit: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.DEPOSIT);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    a.setBalance(a.getBalance() + transaction.getAmount());
                    transaction.setNewBalance(a.getBalance());
                    return accountService.update(a.getId(), a)
                            .flatMap(ac -> transactionRepository.save(transaction))
                            .flatMap(t -> Mono.just("Debit Card Deposit done, new balance: " + a.getBalance()));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    @Override
    public Mono<String> cardPurchase(TransactionDTO transactionDTO) {
        log.info("Making a debit card purchase: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.WITHDRAW);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    a.setBalance(a.getBalance() - transaction.getAmount());
                    if (a.getBalance() < 0) {
                        return Mono.error(new IllegalArgumentException("Insufficient balance for debit card purchase"));
                    }
                    transaction.setNewBalance(a.getBalance());
                    return accountService.update(a.getId(), a)
                            .flatMap(ac -> transactionRepository.save(transaction))
                            .flatMap(t -> Mono.just("Debit card purchase done, new balance: " + a.getBalance()));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    @Override
    public Flux<Transaction> findAllByAccountId(Long accountId) {
        log.info("Listing all transactions by account id");
        return transactionRepository.findAllByAccountId(accountId);
    }

    @Override
    public Mono<Double> getFeeInAPeriod(Long accountId, PeriodDTO periodDTO) {
        return findTransactionsAccountPeriod(accountId, periodDTO.getStart(), periodDTO.getEnd())
                .map(transaction -> transaction.getFee())
                .reduce(0.0, (x1, x2) -> x1 + x2);
    }

    @Override
    public Mono<Long> countTransactionsAccountMonth(Long accountId, LocalDateTime date) {
        return findTransactionsAccountMonth(accountId, date).count();
    }

    @Override
    public Flux<Transaction> findTransactionsAccountMonth(Long accountId, LocalDateTime date) {
        return transactionRepository.findAllByAccountIdAndTransactionDateBetween(accountId, date.withDayOfMonth(1), date.with(TemporalAdjusters.lastDayOfMonth()));
    }

    //@Override
    public Flux<Transaction> findTransactionsAccountPeriod(Long accountId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findAllByAccountIdAndTransactionDateBetween(accountId, start, end);
    }

    @Override
    public Mono<String> checkFields(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Account transaction amount must be greater than 0"));
        }
        return transactionRepository.findById(transaction.getId())
                .flatMap(cc -> Mono.error(new IllegalArgumentException("Account transaction id already exists")));
    }

}
