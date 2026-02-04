package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.RegionalExternaDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class RegionalIntegrationService {

    private final RestClient restClient;

    public RegionalIntegrationService(
            RestClient.Builder builder,
            @Value("${api.integration.regional.url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<RegionalExternaDto> buscarRegionaisExternas() {
        return restClient.get()
                .uri("/v1/regionais")
                .retrieve()
                .body(new ParameterizedTypeReference<List<RegionalExternaDto>>() {
                });
    }
}