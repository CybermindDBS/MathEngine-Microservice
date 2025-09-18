package com.cdev.mathengineclient.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_account")
@Data
@ToString
public class UserAccount {
    @Id
    private Long id;
    private String username;
    private String password;

    public UserAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
