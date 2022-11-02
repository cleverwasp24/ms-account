package com.nttdata.bootcamp.msaccount.dto;

import com.nttdata.bootcamp.msaccount.model.Account;
import com.nttdata.bootcamp.msaccount.model.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class CompleteReportDTO {

    private Account account;
    private List<Transaction> transactions;

}
