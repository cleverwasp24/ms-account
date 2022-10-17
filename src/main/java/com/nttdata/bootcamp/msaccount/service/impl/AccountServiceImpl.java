package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.ClientDTO;
import com.nttdata.bootcamp.msaccount.dto.CurrentAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.FixedTermDepositAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.SavingsAccountDTO;
import com.nttdata.bootcamp.msaccount.infrastructure.AccountRepository;
import com.nttdata.bootcamp.msaccount.mapper.AccountDTOMapper;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.model.enums.ClientTypeEnum;
import com.nttdata.bootcamp.msaccount.service.AccountService;
import com.nttdata.bootcamp.msaccount.service.ClientService;
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
    public Mono<Account> findById(Integer id) {
        log.info("Searching account by id: " + id);
        return accountRepository.findById(id);
    }

    @Override
    public Mono<Account> update(Integer id, Account account) {
        log.info("Updating account with id: " + id + " with : " + account.toString());
        return accountRepository.findById(id)
                .flatMap(savedAccount -> {
                    savedAccount.setClientId(account.getClientId());
                    savedAccount.setAccountType(account.getAccountType());
                    savedAccount.setAccountNumber(account.getAccountNumber());
                    savedAccount.setBalance(account.getBalance());
                    savedAccount.setMaxTransactions(account.getMaxTransactions());
                    savedAccount.setMaintenanceFee(account.getMaintenanceFee());
                    savedAccount.setOpeningDate(account.getOpeningDate());
                    savedAccount.setTransactionDay(account.getTransactionDay());
                    savedAccount.setOwners(account.getOwners());
                    savedAccount.setSigners(account.getSigners());
                    return accountRepository.save(savedAccount);
                });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        log.info("Deleting account with id: " + id);
        return accountRepository.deleteById(id);
    }

    @Override
    public Mono<SavingsAccountDTO> createSavingsAccount(SavingsAccountDTO accountDTO) {
        log.info("Creating savings account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.SAVINGS);

        return clientService.findById(account.getClientId())
                .flatMap(c -> {
                    switch (ClientTypeEnum.valueOf(c.getClientType())) {
                        case PERSONAL:
                            return countClientAccounts(account.getClientId())
                                    .flatMap(l -> {
                                        if (l < 1) {
                                            return accountRepository.save(account);
                                        } else {
                                            log.info("Personal clients can have only one account");
                                            return null;
                                        }
                                    });
                        default:
                            log.info("Only personal clients can create savings accounts");
                            return null;
                    }
                }).map(a -> (SavingsAccountDTO) accountDTOMapper.convertToDto(a, AccountTypeEnum.SAVINGS));
    }

    @Override
    public Mono<CurrentAccountDTO> createCurrentAccount(CurrentAccountDTO accountDTO) {
        log.info("Creating current account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.CURRENT);

        return clientService.findById(account.getClientId())
                .flatMap(c -> {
                    switch (ClientTypeEnum.valueOf(c.getClientType())) {
                        case PERSONAL:
                            return countClientAccounts(account.getClientId())
                                    .flatMap(l -> {
                                        if (l < 1) {
                                            return accountRepository.save(account);
                                        } else {
                                            log.info("Personal clients can have only one account");
                                            return null;
                                        }
                                    });
                        case BUSINESS:
                            log.info("Business accounts must have at least one owner");
                            return (account.getOwners() != null && !account.getOwners().isEmpty())
                                    ? accountRepository.save(account) : null;
                        default:
                            return null;
                    }
                })
                .map(c -> (CurrentAccountDTO) accountDTOMapper.convertToDto(c, AccountTypeEnum.CURRENT));
    }

    @Override
    public Mono<FixedTermDepositAccountDTO> createFixedTermDepositAccount(FixedTermDepositAccountDTO accountDTO) {
        log.info("Creating fixed term deposit account: " + accountDTO.toString());
        Account account = accountDTOMapper.convertToEntity(accountDTO, AccountTypeEnum.FIXED_TERM_DEPOSIT);

        return clientService.findById(account.getClientId())
                .flatMap(c -> {
                    switch (ClientTypeEnum.valueOf(c.getClientType())) {
                        case PERSONAL:
                            return countClientAccounts(account.getClientId())
                                    .flatMap(l -> {
                                        if (l < 1) {
                                            return accountRepository.save(account);
                                        } else {
                                            log.info("Personal clients can have only one account");
                                            return null;
                                        }
                                    });
                        default:
                            log.info("Only personal clients can create fixed term deposit accounts");
                            return null;
                    }
                })
                .map(c -> (FixedTermDepositAccountDTO) accountDTOMapper.convertToDto(c, AccountTypeEnum.FIXED_TERM_DEPOSIT));
    }

    @Override
    public Mono<Long> countClientAccounts(Integer clientId) {
        return accountRepository.findAllByClientId(clientId).count();
    }

    @Override
    public Flux<Account> findAllByClientId(Integer id) {
        log.info("Listing all accounts by client id");
        return accountRepository.findAllByClientId(id);
    }
}
