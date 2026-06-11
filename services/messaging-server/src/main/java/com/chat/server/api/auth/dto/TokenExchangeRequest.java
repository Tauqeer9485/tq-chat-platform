package com.chat.server.api.auth.dto;

import java.util.List;

public class TokenExchangeRequest {
    private List<String> scopes;

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }
}   