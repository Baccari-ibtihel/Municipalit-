package com.baladiati.smart.dto;

import lombok.Data;

@Data
public class AgentActivationRequest {
    private String activationCode;
    private String email;
    private String password;
}
