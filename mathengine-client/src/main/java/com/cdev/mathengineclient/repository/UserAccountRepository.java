package com.cdev.mathengineclient.repository;

import com.cdev.mathengineclient.entity.UserAccount;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserAccountRepository extends ReactiveCrudRepository<UserAccount, Long> {
    Mono<UserAccount> findUserAccountByUsername(String username);
}
