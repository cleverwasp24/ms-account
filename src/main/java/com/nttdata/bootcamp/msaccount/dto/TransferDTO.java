package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

@Data
public class TransferDTO {

    private Integer id;
    private Integer accountId;
    private Double amount;
    private Integer destinationAccountId;

}
