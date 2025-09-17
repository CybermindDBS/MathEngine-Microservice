package com.cdev.mathengineclient.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_functions")
@Data
@RequiredArgsConstructor
public class UserFunctions {
    @Id
    private Long id;
    private String userId;
    private String uuid;
    private String functions;
}
