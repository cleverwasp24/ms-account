package com.nttdata.bootcamp.msaccount.infrastructure;

import com.nttdata.bootcamp.msaccount.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<Account, Long> {

    Flux<Account> findAllByClientId(Long id);

    Flux<Account> findAllByClientIdAndAccountType(Long clientId, Integer accountType);

}
