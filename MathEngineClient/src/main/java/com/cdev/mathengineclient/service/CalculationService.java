package com.cdev.mathengineclient.service;

import com.cdev.mathengineclient.entity.Calculation;
import com.cdev.mathengineclient.entity.UserFunctions;
import com.cdev.mathengineclient.repository.CalculationRepository;
import com.cdev.mathengineclient.repository.UserFunctionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CalculationService {
    @Autowired
    CalculationRepository calculationRepository;
    @Autowired
    UserFunctionsRepository userFunctionsRepository;

    public Flux<Calculation> getCalculations(String username, String UUID, int limit, int offset) {
        if (username != null)
            return calculationRepository.findAllByUserIdPaginated(username, limit, offset);
        else {
//            return calculationRepository.findAllByUuid(UUID);
            return calculationRepository.findAllByUuidPaginated(UUID, limit, offset);
        }
    }

    public Mono<Calculation> getLatestCalculation(String username, String UUID) {
        if (username != null)
            return calculationRepository.findLatestCalculationByUserId(username);
        else {
            return calculationRepository.findLatestCalculationByUuid(UUID);
        }
    }

    public Mono<Calculation> getCalculation(Long id, String userId, String UUID) {

        return calculationRepository.findCalculationById(id).flatMap(calculation -> {
            String calcUserId = calculation.getUserId();
            String calcUuid = calculation.getUuid();
            if ((calcUserId != null && calcUserId.equals(userId)) || (calcUuid != null && calcUuid.equals(UUID)))
                return Mono.just(calculation);
            else return Mono.<Calculation>empty();
        });
    }

    public Mono<Calculation> createNewCalculation(String username, String UUID) {
        Calculation calculation = new Calculation();
        calculation.setUserId(username);
        calculation.setUuid(UUID);
        return calculationRepository.save(calculation);
    }

    public Mono<Calculation> updateCalculation(Calculation calculation) {
        return calculationRepository.save(calculation);
    }

    public Mono<Long> getTotalCalculationsCount(String username, String UUID) {
        if (username != null)
            return calculationRepository.countAllByUserId(username);
        else {
            return calculationRepository.countAllByUuid(UUID);
        }
    }

    public Mono<UserFunctions> saveFunctions(String functions, String username, String UUID) {
        return getFunctions(username, UUID).flatMap(userFunctions -> {
            userFunctions.setFunctions(functions);
            return userFunctionsRepository.save(userFunctions);
        }).switchIfEmpty(Mono.defer(() -> {
            UserFunctions userFunctions = new UserFunctions();
            userFunctions.setUserId(username);
            userFunctions.setUuid(UUID);
            userFunctions.setFunctions(functions);
            return userFunctionsRepository.save(userFunctions).map(userFunctions1 -> {
                return userFunctions1;
            });
        }));
    }

    public Mono<UserFunctions> getFunctions(String username, String UUID) {
        if (username != null)
            return userFunctionsRepository.findUserFunctionsByUserId(username);
        else {

            return userFunctionsRepository.findUserFunctionsByUuid(UUID);
        }
    }
}
