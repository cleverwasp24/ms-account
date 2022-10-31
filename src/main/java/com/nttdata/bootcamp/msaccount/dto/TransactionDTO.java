package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

@Data
public class TransactionDTO {

    private Long accountId;
    private Double amount;

}
