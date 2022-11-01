package com.nttdata.bootcamp.msaccount.mapper;

import com.nttdata.bootcamp.msaccount.dto.*;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import com.nttdata.bootcamp.msaccount.model.enums.TransactionTypeEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class TransactionDTOMapper {

    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public Object convertToDto(Transaction transaction, TransactionTypeEnum type) {
        return switch (type) {
            case DEPOSIT, WITHDRAW, CARD_DEPOSIT, CARD_PURCHASE -> modelMapper.map(transaction, TransactionDTO.class);
            case TRANSFER_OWN, TRANSFER_THIRD -> modelMapper.map(transaction, TransferDTO.class);
        };
    }

    public Transaction convertToEntity(Object transactionDTO, TransactionTypeEnum type) {
        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(type.ordinal());
        transaction.setFee(0.0);

        switch (type) {
            case DEPOSIT -> transaction.setDescription("ACCOUNT DEPOSIT +$ " + transaction.getAmount());
            case WITHDRAW -> transaction.setDescription("ACCOUNT WITHDRAW -$ " + transaction.getAmount());
            case TRANSFER_OWN -> transaction.setDescription("SEND OWN ACCOUNT TRANSFER -$ " + transaction.getAmount());
            case TRANSFER_THIRD ->
                    transaction.setDescription("SEND THIRD ACCOUNT TRANSFER -$ " + transaction.getAmount());
            case CARD_DEPOSIT -> transaction.setDescription("DEBIT CARD DEPOSIT +$ " + transaction.getAmount());
            case CARD_PURCHASE -> transaction.setDescription("DEBIT CARD PURCHASE -$ " + transaction.getAmount());
        }

        return transaction;
    }

    public Transaction generateDestinationAccountTransaction(Transaction transaction) {
        Transaction destinationTransaction = modelMapper.map(transaction, Transaction.class);
        destinationTransaction.setAccountId(transaction.getDestinationAccountId());
        switch (TransactionTypeEnum.valueOf(transaction.getTransactionType())) {
            case TRANSFER_OWN ->
                    destinationTransaction.setDescription("RECEIVE OWN ACCOUNT TRANSFER +$ " + transaction.getAmount());
            case TRANSFER_THIRD ->
                    destinationTransaction.setDescription("RECEIVE THIRD ACCOUNT TRANSFER +$ " + transaction.getAmount());
        }
        return destinationTransaction;
    }
}
