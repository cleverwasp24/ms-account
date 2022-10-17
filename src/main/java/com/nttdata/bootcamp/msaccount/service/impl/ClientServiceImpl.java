package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.ClientDTO;
import com.nttdata.bootcamp.msaccount.service.ClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class ClientServiceImpl implements ClientService {

    private final WebClient webClient;

    public ClientServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();//microservicio cliente
    }

    @Override
    public Mono<ClientDTO> findById(Integer id) {
        ClientDTO client = new ClientDTO();
        Mono<ClientDTO> clientById = this.webClient.get()
                .uri("/bootcamp/client/{id}", id)
                .retrieve()
                .bodyToMono(ClientDTO.class);

        log.info("Client obtained from service ms-client:" + clientById);
        return clientById.flatMap(x -> {
            client.setId(x.getId());
            client.setClientType(x.getClientType());
            return Mono.just(client);
        });
    }

}
