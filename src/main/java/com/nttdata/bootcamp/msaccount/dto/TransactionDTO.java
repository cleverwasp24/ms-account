package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

@Data
public class TransactionDTO {

    private Integer id;
    private Integer accountId;
    private Integer transactionType;
    private String description;
    private Double amount;

}
