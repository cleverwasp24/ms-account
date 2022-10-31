package com.nttdata.bootcamp.msaccount.dto;

import com.nttdata.bootcamp.msaccount.model.AuthorizedSigner;
import com.nttdata.bootcamp.msaccount.model.Owner;
import lombok.Data;

import java.util.List;

@Data
public class CurrentAccountDTO {

    private Long clientId;
    private String accountNumber;
    private Double balance;
    private List<Owner> owners;
    private List<AuthorizedSigner> signers;

}
