package com.alterronix.alteronixback.dto;

public record GoogleTokenResponse(String access_token,
                                  String refresh_token,
                                  Integer expires_in,
                                  String scope,
                                  String token_type) {
}
