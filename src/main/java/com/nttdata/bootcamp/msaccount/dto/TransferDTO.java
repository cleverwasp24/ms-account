package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

@Data
public class TransferDTO {

    private Long accountId;
    private Double amount;
    private Long destinationAccountId;

}
