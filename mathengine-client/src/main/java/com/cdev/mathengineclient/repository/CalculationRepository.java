package com.cdev.mathengineclient.repository;

import com.cdev.mathengineclient.entity.Calculation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CalculationRepository extends ReactiveCrudRepository<Calculation, Long> {
    Mono<Calculation> findCalculationById(Long id);

    Flux<Calculation> findAllByUuid(String Uuid);

    @Query("select * from calculations where uuid = :uuid order by id desc limit :limit offset :offset")
    Flux<Calculation> findAllByUuidPaginated(String uuid, int limit, int offset);

    @Query("select * from calculations where uuid = :uuid order by id desc limit 1")
    Mono<Calculation> findLatestCalculationByUuid(String uuid);

    Mono<Long> countAllByUuid(String Uuid);

    Flux<Calculation> findAllByUserId(String userId);

    @Query("select * from calculations where user_id = :userId order by id desc limit :limit offset :offset")
    Flux<Calculation> findAllByUserIdPaginated(String userId, int limit, int offset);

    @Query("select * from calculations where user_id = :userId order by id desc limit 1")
    Mono<Calculation> findLatestCalculationByUserId(String userId);

    Mono<Long> countAllByUserId(String userId);
}
