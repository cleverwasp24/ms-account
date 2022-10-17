package com.nttdata.bootcamp.msaccount.service;

import com.nttdata.bootcamp.msaccount.dto.ClientDTO;
import reactor.core.publisher.Mono;

public interface ClientService {

    Mono<ClientDTO> findById(Integer id);

}
