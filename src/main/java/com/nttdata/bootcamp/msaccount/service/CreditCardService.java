package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.CreditCardDTO;
import reactor.core.publisher.Flux;

public interface CreditCardService {

    Flux<CreditCardDTO> findAllById(Long id);

}
