package br.com.sicred.assemblyvote.client;

import br.com.sicred.assemblyvote.client.response.DocumentStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "validate-member", url = "${feign.client.validate-member.url}")
public interface ValidateMemberClient {

    @GetMapping("/v1/users/{cpf}")
    DocumentStatusResponse getStatus(@PathVariable String cpf);
}
