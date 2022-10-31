package com.nttdata.bootcamp.msaccount.service;

import reactor.core.publisher.Mono;

public interface DatabaseSequenceService {

    Mono<Long> generateSequence(String seqName);

}
