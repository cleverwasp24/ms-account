package com.nttdata.bootcamp.msaccount.mapper;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.model.enums.AccountTypeEnum;
import com.nttdata.bootcamp.msaccount.util.Constants;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class AccountDTOMapper {
    
    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public Object convertToDto(Account account, AccountTypeEnum type) {
        return switch (type) {
            case SAVINGS -> modelMapper.map(account, SavingsAccountDTO.class);
            case CURRENT -> modelMapper.map(account, CurrentAccountDTO.class);
            case FIXED_TERM_DEPOSIT -> modelMapper.map(account, FixedTermDepositAccountDTO.class);
            case VIP -> modelMapper.map(account, VIPAccountDTO.class);
            case PYME -> modelMapper.map(account, PYMEAccountDTO.class);
        };
    }
    public Account convertToEntity(Object accountDTO, AccountTypeEnum type) {
        Account account = modelMapper.map(accountDTO, Account.class);
        account.setAccountType(type.ordinal());
        account.setOpeningDate(LocalDateTime.now());
        account.setMaintenanceFee(0.00);
        switch (type) {
            case SAVINGS -> {
                account.setMaxFreeTransactions(Constants.SAVINGS_MAX_FREE_TRANSACTIONS);
                account.setTransactionFee(Constants.SAVINGS_TRANSACTION_FEE);
            }
            case CURRENT -> {
                account.setMaxFreeTransactions(Constants.CURRENT_MAX_FREE_TRANSACTIONS);
                account.setTransactionFee(Constants.CURRENT_TRANSACTION_FEE);
                account.setMaintenanceFee(Constants.CURRENT_MAINTENANCE_FEE);
            }
            case FIXED_TERM_DEPOSIT -> {
                account.setMaxFreeTransactions(Constants.FIXED_TERM_DEPOSIT_MAX_TRANSACTIONS);
                account.setTransactionFee(Constants.FIXED_TERM_DEPOSIT_TRANSACTION_FEE);
            }
            case VIP -> {
                account.setMaxFreeTransactions(Constants.VIP_MAX_TRANSACTIONS);
                account.setTransactionFee(Constants.VIP_TRANSACTION_FEE);
            }
            case PYME -> {
                account.setMaxFreeTransactions(Constants.PYME_MAX_TRANSACTIONS);
                account.setTransactionFee(Constants.PYME_TRANSACTION_FEE);
            }
        }
        return account;
    }
    
}
