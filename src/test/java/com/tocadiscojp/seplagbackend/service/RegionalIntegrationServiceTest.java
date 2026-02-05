package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.RegionalExternaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RegionalIntegrationService")
class RegionalIntegrationServiceTest {

    @Mock
    private RestClient.Builder builder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private RegionalIntegrationService integrationService;

    private static final String BASE_URL = "http://api.test.com";

    @BeforeEach
    void setUp() {
        when(builder.baseUrl(BASE_URL)).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        integrationService = new RegionalIntegrationService(builder, BASE_URL);
    }

    @Test
    @DisplayName("Deve buscar regionais externas com sucesso")
    void buscarRegionaisExternas_ComSucesso_RetornaLista() {
        // Arrange
        RegionalExternaDto regional1 = new RegionalExternaDto(1, "Regional Norte");
        RegionalExternaDto regional2 = new RegionalExternaDto(2, "Regional Sul");
        List<RegionalExternaDto> regionaisEsperadas = Arrays.asList(regional1, regional2);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/v1/regionais")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(regionaisEsperadas);

        // Act
        List<RegionalExternaDto> resultado = integrationService.buscarRegionaisExternas();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nome()).isEqualTo("Regional Norte");
        assertThat(resultado.get(1).nome()).isEqualTo("Regional Sul");
        verify(restClient).get();
    }

    @Test
    @DisplayName("Deve construir RestClient com URL base correta")
    void construtor_ConfiguraRestClientComUrlBase() {
        // Assert
        verify(builder).baseUrl(BASE_URL);
        verify(builder).build();
    }
}
