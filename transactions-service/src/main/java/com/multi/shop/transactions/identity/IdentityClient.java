package com.multi.shop.transactions.identity;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "identity", url = "${identity.service.url}")
public interface IdentityClient {
    @GetMapping("/internal/accounts")
    Account findByIdentity(@RequestParam("identity") String identity);

    @GetMapping("/internal/accounts")
    Account findByEmail(@RequestParam("email") String email);
}
