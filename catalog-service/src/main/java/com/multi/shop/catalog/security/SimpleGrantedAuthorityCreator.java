package com.multi.shop.catalog.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SimpleGrantedAuthorityCreator {
    @JsonCreator
    public SimpleGrantedAuthorityCreator(@JsonProperty("authority") String role) {};
}
