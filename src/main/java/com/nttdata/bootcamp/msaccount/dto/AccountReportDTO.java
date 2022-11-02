package com.nttdata.bootcamp.msaccount.dto;

import com.nttdata.bootcamp.msaccount.model.Account;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountReportDTO {

    private Account account;
    private List<DailyBalanceDTO> dailyBalances = new ArrayList<>();

}
