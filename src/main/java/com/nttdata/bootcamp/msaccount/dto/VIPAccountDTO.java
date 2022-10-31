package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

@Data
public class VIPAccountDTO {

    private Long clientId;
    private String accountNumber;
    private Double balance;

}
