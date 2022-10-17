package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

@Data
public class SavingsAccountDTO {

    private Integer id;
    private Integer clientId;
    private String accountNumber;
    private Double balance;

}
