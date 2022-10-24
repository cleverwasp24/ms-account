package com.nttdata.bootcamp.msaccount.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PeriodDTO {

    private LocalDateTime start;
    private LocalDateTime end;

}
