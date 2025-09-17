package com.cdev.mathengineclient.repository;

import com.cdev.mathengineclient.entity.UserFunctions;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserFunctionsRepository extends ReactiveCrudRepository<UserFunctions, Long> {
    Mono<UserFunctions> findUserFunctionsByUserId(String username);

    Mono<UserFunctions> findUserFunctionsByUuid(String username);
}
