package com.baladiati.smart.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String nationalId;
    private String phone;
    private String email;
    private String password;
}
