package com.nttdata.bootcamp.msaccount.service.impl;

import com.nttdata.bootcamp.msaccount.dto.CreditCardDTO;
import com.nttdata.bootcamp.msaccount.service.CreditCardService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class CreditCardServiceImpl implements CreditCardService {

    private final WebClient webClient;

    public CreditCardServiceImpl(WebClient.Builder webClientBuilder) {
        //microservicio credits
        this.webClient = webClientBuilder.baseUrl("http://localhost:8083").build();
    }

    @CircuitBreaker(name = "service-credit-card", fallbackMethod = "findAllByIdFallback")
    @TimeLimiter(name = "service-credit-card")
    @Override
    public Flux<CreditCardDTO> findAllById(Long id){
        return this.webClient.get()
                .uri("/bootcamp/creditcard/findAllByClientId/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .bodyToFlux(CreditCardDTO.class);
    }

    public Flux<CreditCardDTO> findAllByIdFallback(Long id, Throwable t) {
        log.info("Fallback method for findAllByIdFallback (CREDIT CARD) executed {}", t.getMessage());
        return Flux.empty();
    }

}
