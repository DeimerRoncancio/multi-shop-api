package com.multi.shop.api.multi_shop_api.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SimpleGrantedAuthorityCreator {

    @JsonCreator
    public SimpleGrantedAuthorityCreator(@JsonProperty("authority") String role) {};
}
