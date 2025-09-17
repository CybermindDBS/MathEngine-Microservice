package com.cdev.mathengineclient.security;

import com.cdev.mathengineclient.entity.UserAccount;
import com.cdev.mathengineclient.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserDetailsService implements ReactiveUserDetailsService {
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Mono<UserAccount> userAccount = userAccountRepository.findUserAccountByUsername(username);
        return userAccount.map(user -> User.builder().username(user.getUsername())
                .password(user.getPassword())
                .build()
        );
    }
}
