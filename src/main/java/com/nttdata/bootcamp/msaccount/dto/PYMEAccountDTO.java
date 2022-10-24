package com.nttdata.bootcamp.msaccount.dto;

import com.nttdata.bootcamp.msaccount.model.AuthorizedSigner;
import com.nttdata.bootcamp.msaccount.model.Owner;
import lombok.Data;

import java.util.List;

@Data
public class PYMEAccountDTO {

    private Integer id;
    private Integer clientId;
    private String accountNumber;
    private Double balance;
    private Double maintenanceFee;
    private List<Owner> owners;
    private List<AuthorizedSigner> signers;

}
