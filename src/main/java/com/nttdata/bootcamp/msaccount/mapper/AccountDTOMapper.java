package com.nttdata.bootcamp.msaccount.mapper;

import com.nttdata.bootcamp.msaccount.dto.CurrentAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.FixedTermDepositAccountDTO;
import com.nttdata.bootcamp.msaccount.dto.SavingsAccountDTO;
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
        switch (type){
            case SAVINGS:
                return modelMapper.map(account, SavingsAccountDTO.class);
            case CURRENT:
                return modelMapper.map(account, CurrentAccountDTO.class);
            case FIXED_TERM_DEPOSIT:
                return modelMapper.map(account, FixedTermDepositAccountDTO.class);
            default:
                return null;
        }
    }
    public Account convertToEntity(Object accountDTO, AccountTypeEnum type) {
        Account account = modelMapper.map(accountDTO, Account.class);
        account.setAccountType(type.ordinal());
        account.setOpeningDate(LocalDateTime.now());
        switch (type){
            case SAVINGS:
                account.setMaxTransactions(Constants.SAVINGS_ACCOUNT_MAX_TRANSACTIONS);
                break;
            case FIXED_TERM_DEPOSIT:
                account.setMaxTransactions(Constants.FIXED_TERM_DEPOSIT_ACCOUNT_MAX_TRANSACTIONS);
                break;
        }
        return account;
    }
    
}
