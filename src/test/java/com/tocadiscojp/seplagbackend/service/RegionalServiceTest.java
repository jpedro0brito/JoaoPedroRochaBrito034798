package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.RegionalExternaDto;
import com.tocadiscojp.seplagbackend.model.Regional;
import com.tocadiscojp.seplagbackend.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RegionalService")
class RegionalServiceTest {

    @Mock
    private RegionalRepository repository;

    @Mock
    private RegionalIntegrationService integrationService;

    @InjectMocks
    private RegionalService regionalService;

    private Regional regional1;
    private Regional regional2;

    @BeforeEach
    void setUp() {
        regional1 = new Regional(1, "Regional Norte");
        regional1.setId(1L);
        regional1.setAtivo(true);

        regional2 = new Regional(2, "Regional Sul");
        regional2.setId(2L);
        regional2.setAtivo(true);
    }

    @Test
    @DisplayName("Deve criar novas regionais quando não existem localmente")
    void sincronizarRegionais_ComNovasRegionais_CriaNovasRegionais() {
        // Arrange
        RegionalExternaDto externa1 = new RegionalExternaDto(3, "Regional Leste");
        RegionalExternaDto externa2 = new RegionalExternaDto(4, "Regional Oeste");

        when(integrationService.buscarRegionaisExternas())
                .thenReturn(Arrays.asList(externa1, externa2));
        when(repository.findByAtivoTrue()).thenReturn(Collections.emptyList());

        // Act
        String resultado = regionalService.sincronizarRegionais();

        // Assert
        assertThat(resultado).contains("Criados: 2");
        assertThat(resultado).contains("Atualizados: 0");
        assertThat(resultado).contains("Inativados: 0");
        verify(repository, times(2)).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve inativar regional antiga e criar nova quando nome muda")
    void sincronizarRegionais_ComRegionaisAlteradas_InativaAntigaECriaNova() {
        // Arrange
        RegionalExternaDto externaAlterada = new RegionalExternaDto(1, "Regional Norte Atualizada");

        when(integrationService.buscarRegionaisExternas())
                .thenReturn(Arrays.asList(externaAlterada));
        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(regional1));

        // Act
        String resultado = regionalService.sincronizarRegionais();

        // Assert
        assertThat(resultado).contains("Criados: 0");
        assertThat(resultado).contains("Atualizados: 1");
        assertThat(resultado).contains("Inativados: 0");
        assertThat(regional1.isAtivo()).isFalse();
        verify(repository, times(2)).save(any(Regional.class)); // 1 inativação + 1 criação
    }

    @Test
    @DisplayName("Deve inativar regionais que não existem mais na API externa")
    void sincronizarRegionais_ComRegionaisRemovidas_InativaRegionais() {
        // Arrange
        when(integrationService.buscarRegionaisExternas()).thenReturn(Collections.emptyList());
        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(regional1, regional2));

        // Act
        String resultado = regionalService.sincronizarRegionais();

        // Assert
        assertThat(resultado).contains("Criados: 0");
        assertThat(resultado).contains("Atualizados: 0");
        assertThat(resultado).contains("Inativados: 2");
        assertThat(regional1.isAtivo()).isFalse();
        assertThat(regional2.isAtivo()).isFalse();
        verify(repository, times(2)).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve processar corretamente mix de criações, atualizações e inativações")
    void sincronizarRegionais_ComMistoDeMudancas_ProcessaCorretamente() {
        // Arrange
        RegionalExternaDto externa1 = new RegionalExternaDto(1, "Regional Norte"); // Mantém
        RegionalExternaDto externa3 = new RegionalExternaDto(3, "Regional Leste"); // Nova
        RegionalExternaDto externa4 = new RegionalExternaDto(2, "Regional Sul Atualizada"); // Atualizada

        when(integrationService.buscarRegionaisExternas())
                .thenReturn(Arrays.asList(externa1, externa3, externa4));
        when(repository.findByAtivoTrue()).thenReturn(Arrays.asList(regional1, regional2));

        // Act
        String resultado = regionalService.sincronizarRegionais();

        // Assert
        assertThat(resultado).contains("Criados: 1"); // Regional Leste
        assertThat(resultado).contains("Atualizados: 1"); // Regional Sul
        assertThat(resultado).contains("Inativados: 0");
        verify(repository, atLeast(2)).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve retornar mensagem com contadores corretos")
    void sincronizarRegionais_RetornaMensagemComContadores() {
        // Arrange
        when(integrationService.buscarRegionaisExternas()).thenReturn(Collections.emptyList());
        when(repository.findByAtivoTrue()).thenReturn(Collections.emptyList());

        // Act
        String resultado = regionalService.sincronizarRegionais();

        // Assert
        assertThat(resultado).isEqualTo("Sincronização concluída. Criados: 0, Atualizados: 0, Inativados: 0");
    }

    @Test
    @DisplayName("Deve listar todas as regionais")
    void listarTodas_RetornaTodasRegionais() {
        // Arrange
        List<Regional> regionais = Arrays.asList(regional1, regional2);
        when(repository.findAll()).thenReturn(regionais);

        // Act
        List<Regional> resultado = regionalService.listarTodas();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(regional1, regional2);
        verify(repository).findAll();
    }
}
