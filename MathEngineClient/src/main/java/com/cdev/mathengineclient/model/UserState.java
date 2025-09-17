package com.cdev.mathengineclient.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor()
public class UserState {

    String username;
    String UUID;
    boolean isOAuth2User;

    public UserState(String username, String UUID, boolean isOAuth2User) {
        this.username = username;
        this.UUID = UUID;
        this.isOAuth2User = isOAuth2User;
    }
}
