package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.msaccount.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.model.enums.TransactionTypeEnum;
import com.nttdata.bootcamp.msaccount.service.AccountService;
import com.nttdata.bootcamp.msaccount.service.DatabaseSequenceService;
import com.nttdata.bootcamp.msaccount.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

@Log4j2
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private DatabaseSequenceService databaseSequenceService;

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

    /**
     * Este metodo realiza un deposito en la cuenta indicada
     *
     * @param transactionDTO
     * @return
     */
    @Override
    public Mono<String> deposit(TransactionDTO transactionDTO) {
        log.info("Making a deposit: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.DEPOSIT);
        //Validar los datos de la transaccion
        return checkFields(transaction)
                //Si los datos son correctos, validar que la cuenta exista
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    //Si la cuenta existe, contar las transacciones de esa cuenta en el mes actual
                    return countTransactionsAccountMonth(transaction.getAccountId(), transaction.getTransactionDate()).flatMap(count -> {
                        //Si el tipo de cuenta es deposito a plazo fijo, solo se permite una transaccion por mes en un día específico del mes
                        if (a.getAccountType().equals(AccountTypeEnum.FIXED_TERM_DEPOSIT.ordinal())
                                && a.getTransactionDay().toLocalDate().getDayOfMonth() !=
                                transaction.getTransactionDate().toLocalDate().getDayOfMonth()) {
                            return Mono.error(new Exception("You can make only one transaction on " + a.getTransactionDay()));
                        }
                        //Si ha superado el número de transacciones gratuitas, se cobra una comisión
                        if (a.getMaxFreeTransactions() <= count) {
                            //Aplicar la comisión correspondiente al tipo de cuenta
                            transaction.calculateFee(AccountTypeEnum.valueOf(a.getAccountType()));
                        }
                        //Se actualiza el saldo de la cuenta
                        a.setBalance(a.getBalance() + transaction.getAmount() - transaction.getFee());
                        if (a.getBalance() < 0) {
                            //Si el saldo es negativo, se cancela la transacción
                            return Mono.error(new IllegalArgumentException("Transaction fee couldn't be paid"));
                        }
                        transaction.setNewBalance(a.getBalance());
                        //Se actualiza la cuenta con el nuevo saldo
                        return accountService.update(a.getId(), a)
                                //Se genera el id de la transacción y se crea la transacción
                                .flatMap(ac -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    transaction.setId(id);
                                    return transactionRepository.save(transaction);
                                }))
                                .flatMap(t -> Mono.just("Deposit done, new balance: " + a.getBalance()));
                    });
                    //Si la cuenta no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    /**
     * Este metodo realiza un retiro en la cuenta indicada
     *
     * @param transactionDTO
     * @return
     */
    @Override
    public Mono<String> withdraw(TransactionDTO transactionDTO) {
        log.info("Making a withdraw: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.WITHDRAW);
        //Validar los datos de la transaccion
        return checkFields(transaction)
                //Si los datos son correctos, validar que la cuenta exista
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    //Si la cuenta existe, contar las transacciones de esa cuenta en el mes actual
                    return countTransactionsAccountMonth(transaction.getAccountId(), transaction.getTransactionDate()).flatMap(count -> {
                        //Si el tipo de cuenta es deposito a plazo fijo, solo se permite una transaccion por mes en un día específico del mes
                        if (a.getAccountType().equals(AccountTypeEnum.FIXED_TERM_DEPOSIT.ordinal())
                                && a.getTransactionDay().toLocalDate().getDayOfMonth() !=
                                transaction.getTransactionDate().toLocalDate().getDayOfMonth()) {
                            return Mono.error(new Exception("You can make only one transaction on " + a.getTransactionDay()));
                        }
                        //Si ha superado el número de transacciones gratuitas, se cobra una comisión
                        if (a.getMaxFreeTransactions() <= count) {
                            //Aplicar la comisión correspondiente al tipo de cuenta
                            transaction.calculateFee(AccountTypeEnum.valueOf(a.getAccountType()));
                        }
                        //Se actualiza el saldo de la cuenta
                        a.setBalance(a.getBalance() - transaction.getAmount() - transaction.getFee());
                        if (a.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to withdraw"));
                        }
                        transaction.setNewBalance(a.getBalance());
                        //Se actualiza la cuenta con el nuevo saldo
                        return accountService.update(a.getId(), a)
                                //Se genera el id de la transacción y se crea la transacción
                                .flatMap(ac -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    transaction.setId(id);
                                    return transactionRepository.save(transaction);
                                }))
                                .flatMap(t -> Mono.just("Withdraw done, new balance: " + a.getBalance()));
                    });
                    //Si la cuenta no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    /**
     * Este metodo realiza una transferencia hacia cuentas propias
     *
     * @param transferDTO
     * @return
     */
    @Override
    public Mono<String> transferOwnAccount(TransferDTO transferDTO) {
        log.info("Own Account transfer: " + transferDTO.toString() + " destination account: " + transferDTO.getDestinationAccountId());
        Transaction transaction = transactionDTOMapper.convertToEntity(transferDTO, TransactionTypeEnum.TRANSFER_OWN);
        //Validar los datos de la transferencia
        return checkFields(transaction)
                //Si los datos son correctos, validar que la cuenta origen exista
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(originAccount -> {
                    //Si la cuenta origen existe, validar que la cuenta destino exista
                    return accountService.findById(transaction.getDestinationAccountId()).flatMap(destinationAccount -> {
                        //Si la cuenta destino existe, verificar que la cuenta origen tenga el saldo suficiente para realizar la transferencia
                        originAccount.setBalance(originAccount.getBalance() - transaction.getAmount());
                        if (originAccount.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to transfer"));
                        }
                        transaction.setNewBalance(originAccount.getBalance());
                        destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                        //Crear una transacción en la cuenta destino
                        Transaction destinationAccountTransaction = transactionDTOMapper.generateDestinationAccountTransaction(transaction);
                        destinationAccountTransaction.setNewBalance(destinationAccount.getBalance());
                        //Actualizar cuenta origen
                        return accountService.update(originAccount.getId(), originAccount)
                                //Generar id de la transacción y registrar la transacción en la cuenta origen
                                .flatMap(oa -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    transaction.setId(id);
                                    return transactionRepository.save(transaction);
                                }))
                                //Actualizar cuenta destino
                                .flatMap(ot -> accountService.update(destinationAccount.getId(), destinationAccount))
                                //Generar id de la transacción y registrar la transacción en la cuenta destino
                                .flatMap(da -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    destinationAccountTransaction.setId(id);
                                    return transactionRepository.save(destinationAccountTransaction);
                                }))
                                .flatMap(dt -> Mono.just("Transfer to own account done, new balance: " + originAccount.getBalance()));
                        //Si la cuenta destino no existe, se cancela la transacción
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Destination account not found")));
                    //Si la cuenta origen no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Origin Account not found"))));
    }

    /**
     * Este metodo realiza una transferencia hacia cuentas de terceros
     *
     * @param transferDTO
     * @return
     */
    @Override
    public Mono<String> transferThirdAccount(TransferDTO transferDTO) {
        log.info("Third Account transfer: " + transferDTO.toString() + " destination account: " + transferDTO.getDestinationAccountId());
        Transaction transaction = transactionDTOMapper.convertToEntity(transferDTO, TransactionTypeEnum.TRANSFER_THIRD);
        //Validar los datos de la transferencia
        return checkFields(transaction)
                //Si los datos son correctos, validar que la cuenta origen exista
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(originAccount -> {
                    //Si la cuenta origen existe, verificar que la cuenta origen tenga el saldo suficiente para realizar la transferencia
                    return accountService.findById(transaction.getDestinationAccountId()).flatMap(destinationAccount -> {
                        originAccount.setBalance(originAccount.getBalance() - transaction.getAmount());
                        if (originAccount.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to transfer"));
                        }
                        transaction.setNewBalance(originAccount.getBalance());
                        destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                        //Crear una transacción en la cuenta destino
                        Transaction destinationAccountTransaction = transactionDTOMapper.generateDestinationAccountTransaction(transaction);
                        destinationAccountTransaction.setNewBalance(destinationAccount.getBalance());
                        //Actualizar cuenta origen
                        return accountService.update(originAccount.getId(), originAccount)
                                //Generar id de la transacción y registrar la transacción en la cuenta origen
                                .flatMap(oa -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    transaction.setId(id);
                                    return transactionRepository.save(transaction);
                                }))
                                //Actualizar cuenta destino
                                .flatMap(ot -> accountService.update(destinationAccount.getId(), destinationAccount))
                                //Generar id de la transacción y registrar la transacción en la cuenta destino
                                .flatMap(da -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    destinationAccountTransaction.setId(id);
                                    return transactionRepository.save(destinationAccountTransaction);
                                }))
                                .flatMap(dt -> Mono.just("Transfer to third account done, new balance: " + originAccount.getBalance()));
                        //Si la cuenta destino no existe, se cancela la transacción
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Destination account not found")));
                    //Si la cuenta origen no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Origin Account not found"))));
    }

    /**
     * Este metodo realiza un deposito mediante tarjeta de debito
     *
     * @param transactionDTO
     * @return
     */
    @Override
    public Mono<String> cardDeposit(TransactionDTO transactionDTO) {
        log.info("Making a debit card deposit: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.CARD_DEPOSIT);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    a.setBalance(a.getBalance() + transaction.getAmount());
                    transaction.setNewBalance(a.getBalance());
                    return accountService.update(a.getId(), a)
                            .flatMap(ac -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                transaction.setId(id);
                                return transactionRepository.save(transaction);
                            }))
                            .flatMap(t -> Mono.just("Debit Card Deposit done, new balance: " + a.getBalance()));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    /**
     * Este metodo realiza una compra mediante tarjeta de debito
     *
     * @param transactionDTO
     * @return
     */
    @Override
    public Mono<String> cardPurchase(TransactionDTO transactionDTO) {
        log.info("Making a debit card purchase: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.CARD_PURCHASE);
        return checkFields(transaction)
                .switchIfEmpty(accountService.findById(transaction.getAccountId()).flatMap(a -> {
                    a.setBalance(a.getBalance() - transaction.getAmount());
                    if (a.getBalance() < 0) {
                        return Mono.error(new IllegalArgumentException("Insufficient balance for debit card purchase"));
                    }
                    transaction.setNewBalance(a.getBalance());
                    return accountService.update(a.getId(), a)
                            .flatMap(ac -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                transaction.setId(id);
                                return transactionRepository.save(transaction);
                            }))
                            .flatMap(t -> Mono.just("Debit card purchase done, new balance: " + a.getBalance()));
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found"))));
    }

    /**
     * Este metodo lista las transacciones de una cuenta
     *
     * @param accountId
     * @return
     */
    @Override
    public Flux<Transaction> findAllByAccountId(Long accountId) {
        log.info("Listing all transactions by account id");
        return transactionRepository.findAllByAccountId(accountId);
    }

    @Override
    public Flux<Transaction> findAllByAccountIdDesc(Long accountId) {
        log.info("Listing all transactions by account id order by date desc");
        return transactionRepository.findAllByAccountIdOrderByTransactionDateDesc(accountId);
    }

    /**
     * Este metodo calcula el monto total de comisiones en un rango de fechas
     *
     * @param accountId
     * @param periodDTO
     * @return
     */
    @Override
    public Mono<Double> getFeeInAPeriod(Long accountId, PeriodDTO periodDTO) {
        return findTransactionsAccountPeriod(accountId, periodDTO.getStart(), periodDTO.getEnd())
                .map(transaction -> transaction.getFee())
                .reduce(0.0, (x1, x2) -> x1 + x2);
    }

    /**
     * Este metodo cuenta la cantidad de transacciones de una cuenta realizadas en un determinado mes
     *
     * @param accountId
     * @param date
     * @return
     */
    @Override
    public Mono<Long> countTransactionsAccountMonth(Long accountId, LocalDateTime date) {
        return findTransactionsAccountMonth(accountId, date).count();
    }

    /**
     * Este metodo lista las transacciones de una cuenta en un determinado mes
     *
     * @param accountId
     * @param date
     * @return
     */
    @Override
    public Flux<Transaction> findTransactionsAccountMonth(Long accountId, LocalDateTime date) {
        return transactionRepository.findAllByAccountIdAndTransactionDateBetween(accountId,
                date.withDayOfMonth(1).with(LocalTime.MIN), date.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX));
    }

    /**
     * Este metodo lista las transacciones de una cuenta en un rango de fechas
     *
     * @param accountId
     * @param start
     * @param end
     * @return
     */
    @Override
    public Flux<Transaction> findTransactionsAccountPeriod(Long accountId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findAllByAccountIdAndTransactionDateBetween(accountId, start, end);
    }

    /**
     * Este método valida los campos de la transacción
     *
     * @param transaction
     * @return
     */
    @Override
    public Mono<String> checkFields(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Account transaction amount must be greater than 0"));
        }
        return Mono.empty();
    }

    @Override
    public Mono<CompleteReportDTO> generateCompleteReport(Long id, PeriodDTO periodDTO) {
        log.info("Generating complete report in a period: " + periodDTO.getStart() + " - " + periodDTO.getEnd());
        Mono<CompleteReportDTO> completeReportDTOMono = Mono.just(new CompleteReportDTO());
        Mono<Account> accountMono = accountService.findById(id);
        Flux<Transaction> transactionFlux = findTransactionsAccountPeriod(id, periodDTO.getStart(), periodDTO.getEnd());
        return completeReportDTOMono.flatMap(r -> accountMono.map(account -> {
            r.setAccount(account);
            return r;
        }).flatMap(r2 -> transactionFlux.collectList().map(transactions -> {
            r2.setTransactions(transactions);
            return r2;
        })));
    }

    /**
     * Este metodo encuentra la última transacción realizada en una cuenta antes de una fecha
     *
     * @param id
     * @param date
     * @return
     */
    @Override
    public Mono<Transaction> findLastTransactionBefore(Long id, LocalDateTime date) {
        return transactionRepository.findByAccountIdAndTransactionDateBeforeOrderByTransactionDateDesc(id, date)
                .flatMap(t -> Mono.just(t))
                //if it is empty take the account opening balance and creation date
                .switchIfEmpty(accountService.findById(id).flatMap(a -> {
                    Transaction transaction = new Transaction();
                    transaction.setNewBalance(a.getOpeningBalance());
                    transaction.setTransactionDate(a.getOpeningDate());
                    return Mono.just(transaction);
                }));
    }

    @Override
    public Mono<AccountReportDTO> generateAccountReportCurrentMonth(Long id) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).with(LocalTime.MIN);
        log.info("Generating account report for current month: " + start + " - " + now);
        return generateAccountReport(id, new PeriodDTO(start, now));
    }

    @Override
    public Mono<AccountReportDTO> generateAccountReport(Long id, PeriodDTO periodDTO) {
        log.info("Generating account report in a period: " + periodDTO.getStart() + " - " + periodDTO.getEnd());
        Mono<AccountReportDTO> accountReportDTOMono = Mono.just(new AccountReportDTO());
        Mono<Account> accountMono = accountService.findById(id);
        Mono<Transaction> firstBefore = findLastTransactionBefore(id, periodDTO.getStart());
        Flux<Transaction> transactionFlux = findTransactionsAccountPeriod(id, periodDTO.getStart(), periodDTO.getEnd());
        return accountReportDTOMono.flatMap(r -> accountMono.map(account -> {
                    r.setAccount(account);
                    return r;
                }))
                .flatMap(r -> transactionFlux.collectList().map(tl -> {
                    tl = tl.stream().collect(
                                    Collectors.groupingBy(t -> t.getTransactionDate().toLocalDate(),
                                            Collectors.collectingAndThen(
                                                    Collectors.maxBy(
                                                            Comparator.comparing(Transaction::getTransactionDate)),
                                                    transaction -> transaction.get())))
                            .values().stream().collect(Collectors.toList());
                    //Add all transactions to the report as daily balances
                    tl.forEach(t -> r.getDailyBalances().add(new DailyBalanceDTO(t.getTransactionDate().toLocalDate(), t.getNewBalance())));
                    return r;
                }))
                .flatMap(r -> firstBefore.map(t -> {
                    //If transaction list does not contain a transaction on the start date, add it
                    if (r.getDailyBalances().stream().noneMatch(ta -> ta.getDate().equals(periodDTO.getStart().toLocalDate()))) {
                        if (t.getTransactionDate().toLocalDate().equals(periodDTO.getStart().toLocalDate())) {
                            r.getDailyBalances().add(new DailyBalanceDTO(t.getTransactionDate().toLocalDate(), t.getNewBalance()));
                        } else {
                            r.getDailyBalances().add(new DailyBalanceDTO(periodDTO.getStart().toLocalDate(), 0.00));
                        }
                    }
                    return r;
                }))
                //Fill missingDays in the transaction list
                .flatMap(r -> {
                    long days = ChronoUnit.DAYS.between(periodDTO.getStart().toLocalDate(), periodDTO.getEnd().toLocalDate());
                    HashMap<LocalDate, Double> map = new HashMap<>();
                    r.getDailyBalances().forEach(t -> map.put(t.getDate(), t.getBalance()));
                    for (int i = 1; i <= days; i++) {
                        LocalDate date = periodDTO.getStart().toLocalDate().plusDays(i);
                        if (!map.containsKey(date)) {
                            map.put(date, map.get(date.minusDays(1)));
                        }
                    }
                    r.setDailyBalances(new ArrayList<>());
                    map.forEach((k, v) -> r.getDailyBalances().add(new DailyBalanceDTO(k, v)));
                    //Sort the list by date
                    r.getDailyBalances().sort(Comparator.comparing(DailyBalanceDTO::getDate));
                    return Mono.just(r);
                });
    }

}
