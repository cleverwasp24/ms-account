package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.infrastructure.AccountRepository;
import com.nttdata.bootcamp.msaccount.mapper.AccountDTOMapper;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.model.enums.ClientTypeEnum;
import com.nttdata.bootcamp.msaccount.service.AccountService;
import com.nttdata.bootcamp.msaccount.service.CreditCardService;
import com.nttdata.bootcamp.msaccount.service.DatabaseSequenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientServiceImpl clientService;

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private DatabaseSequenceService databaseSequenceService;

    private AccountDTOMapper accountDTOMapper = new AccountDTOMapper();

    @Override
    public Flux<Account> findAll() {
        log.info("Listing all accounts");
        return accountRepository.findAll();
    }

    @Override
    public Mono<Account> create(Account account) {
        log.info("Creating account: " + account.toString());
        return accountRepository.save(account);
    }

    @Override
    public Mono<Account> findById(Long id) {
        log.info("Searching account by id: " + id);
        return accountRepository.findById(id);
    }

    @Override
    public Mono<Account> update(Long id, Account account) {
        log.info("Updating account with id: " + id + " with : " + account.toString());
        return accountRepository.findById(id).flatMap(a -> {
            account.setId(id);
            return accountRepository.save(account);
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting account with id: " + id);
        return accountRepository.deleteById(id);
    }

    @Override
    public Mono<String> createSavingsAccount(SavingsAccountDTO accountDTO) {
        log.info("Creating savings account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.SAVINGS);
        return checkFields(account).switchIfEmpty(clientService.findById(account.getClientId()).flatMap(c -> {
            switch (ClientTypeEnum.valueOf(c.getClientType())) {
                case PERSONAL:
                    return findAllByClientIdAndAccountType(account.getClientId(), AccountTypeEnum.SAVINGS.ordinal()).count().flatMap(l -> (l < 1)
                                    ? databaseSequenceService.generateSequence(Account.SEQUENCE_NAME).flatMap(sequence -> {
                                account.setId(sequence);
                                return accountRepository.save(account)
                                        .flatMap(a -> Mono.just("Savings Account created! " + accountDTOMapper.convertToDto(a, AccountTypeEnum.SAVINGS)));
                            }) : Mono.error(new IllegalArgumentException("Personal clients can have only one savings account"))
                    );
                case BUSINESS:
                    return Mono.error(new IllegalArgumentException("Only personal clients can create savings accounts"));
                default:
                    return Mono.error(new IllegalArgumentException("Invalid client type"));
            }
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Mono<String> createCurrentAccount(CurrentAccountDTO accountDTO) {
        log.info("Creating current account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.CURRENT);
        return checkFields(account).switchIfEmpty(clientService.findById(account.getClientId()).flatMap(c -> {
            switch (ClientTypeEnum.valueOf(c.getClientType())) {
                case PERSONAL:
                    return findAllByClientIdAndAccountType(account.getClientId(), AccountTypeEnum.CURRENT.ordinal()).count().flatMap(l -> (l < 1)
                                    ? databaseSequenceService.generateSequence(Account.SEQUENCE_NAME).flatMap(sequence -> {
                                account.setId(sequence);
                                return accountRepository.save(account)
                                        .flatMap(a -> Mono.just("Current Account created! " + accountDTOMapper.convertToDto(a, AccountTypeEnum.CURRENT)));
                            }) : Mono.error(new IllegalArgumentException("Personal clients can have only one current account"))
                    );
                case BUSINESS:
                    if (account.getOwners() != null && !account.getOwners().isEmpty()) {
                        return databaseSequenceService.generateSequence(Account.SEQUENCE_NAME).flatMap(sequence -> {
                            account.setId(sequence);
                            return accountRepository.save(account).flatMap(a -> Mono.just("Current Account created! " + accountDTOMapper.convertToDto(a, AccountTypeEnum.CURRENT)));
                        });
                    } else {
                        Mono.error(new IllegalArgumentException("Business accounts must have at least one owner"));
                    }
                default:
                    return Mono.error(new IllegalArgumentException("Invalid client type"));
            }
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Mono<String> createFixedTermDepositAccount(FixedTermDepositAccountDTO accountDTO) {
        log.info("Creating fixed term deposit account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.FIXED_TERM_DEPOSIT);
        return checkFields(account).switchIfEmpty(clientService.findById(account.getClientId()).flatMap(c -> {
            switch (ClientTypeEnum.valueOf(c.getClientType())) {
                case PERSONAL:
                    return databaseSequenceService.generateSequence(Account.SEQUENCE_NAME).flatMap(sequence -> {
                        account.setId(sequence);
                        return accountRepository.save(account).flatMap(a -> Mono.just("Fixed Term Deposit Account created! " + accountDTOMapper.convertToDto(a, AccountTypeEnum.FIXED_TERM_DEPOSIT)));
                    });
                case BUSINESS:
                    return Mono.error(new IllegalArgumentException("Only personal clients can create fixed term deposit accounts"));
                default:
                    return Mono.error(new IllegalArgumentException("Invalid client type"));
            }
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Mono<String> createVIPAccount(VIPAccountDTO accountDTO) {
        log.info("Creating VIP account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.VIP);
        return checkFields(account).switchIfEmpty(clientService.findById(account.getClientId()).flatMap(c -> {
            switch (ClientTypeEnum.valueOf(c.getClientType())) {
                case PERSONAL:
                    return findAllByClientIdAndAccountType(account.getClientId(), AccountTypeEnum.VIP.ordinal()).count().flatMap(l -> (l < 1)
                            //VALIDAR SI TIENE TARJETA DE CREDITO
                            ? creditCardService.findAllById(c.getId()).count().flatMap(cc -> (cc < 1)
                            //CREAR CUENTA VIP
                            ? databaseSequenceService.generateSequence(Account.SEQUENCE_NAME).flatMap(sequence -> {
                        account.setId(sequence);
                        return accountRepository.save(account).flatMap(a -> Mono.just("VIP Account created! " + accountDTOMapper.convertToDto(a, AccountTypeEnum.VIP)));
                    })
                            : Mono.error(new IllegalArgumentException("Client must have a credit card in order to create a VIP account")))
                            : Mono.error(new IllegalArgumentException("Personal clients can have only one VIP account")));
                case BUSINESS:
                    return Mono.error(new IllegalArgumentException("Only personal clients can create VIP accounts"));
                default:
                    return Mono.error(new IllegalArgumentException("Invalid client type"));
            }
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Mono<String> createPYMEAccount(PYMEAccountDTO accountDTO) {
        log.info("Creating PYME account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.PYME);
        return checkFields(account).switchIfEmpty(clientService.findById(account.getClientId()).flatMap(c -> {
            switch (ClientTypeEnum.valueOf(c.getClientType())) {
                case PERSONAL:
                    return Mono.error(new IllegalArgumentException("Only business clients can create PYME accounts"));
                case BUSINESS:
                    return (account.getOwners() != null && !account.getOwners().isEmpty())
                            //VALIDAR SI TIENE TARJETA DE CREDITO...
                            ? creditCardService.findAllById(c.getId()).count().flatMap(cc -> (cc < 1)
                            //CREAR CUENTA PYME
                            ? databaseSequenceService.generateSequence(Account.SEQUENCE_NAME).flatMap(sequence -> {
                        account.setId(sequence);
                        return accountRepository.save(account).flatMap(a -> Mono.just("PYME Account created! " + accountDTOMapper.convertToDto(a, AccountTypeEnum.PYME)));
                    })
                            : Mono.error(new IllegalArgumentException("Client must have a credit card in order to create a PYME account")))
                            : Mono.error(new IllegalArgumentException("Business accounts must have at least one owner"));
                default:
                    return Mono.error(new IllegalArgumentException("Invalid client type"));
            }
        }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Flux<Account> findAllByClientId(Long id) {
        log.info("Listing all accounts by client id");
        return accountRepository.findAllByClientId(id);
    }

    @Override
    public Flux<Account> findAllByClientIdAndAccountType(Long id, Integer type) {
        log.info("Listing all accounts by client id and account type");
        return accountRepository.findAllByClientIdAndAccountType(id, type);
    }

    @Override
    public Mono<String> checkFields(Account account) {
        if (account.getAccountNumber() == null || account.getAccountNumber().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Account number cannot be empty"));
        }
        if (account.getBalance() == null || account.getBalance() < 0) {
            return Mono.error(new IllegalArgumentException("New account balance must be equal or greater than 0"));
        }
        return Mono.empty();
    }

}
