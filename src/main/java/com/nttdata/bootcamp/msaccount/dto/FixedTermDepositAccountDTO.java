package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FixedTermDepositAccountDTO {

    private Integer id;
    private Integer clientId;
    private String accountNumber;
    private Double balance;
    private LocalDateTime transactionDay;

}
