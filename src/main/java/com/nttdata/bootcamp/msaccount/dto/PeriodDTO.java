package com.nttdata.bootcamp.msaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PeriodDTO {

    private LocalDateTime start;
    private LocalDateTime end;

}
