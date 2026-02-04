package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.RegionalExternaDto;
import com.tocadiscojp.seplagbackend.model.Regional;
import com.tocadiscojp.seplagbackend.repository.RegionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RegionalService {

    private final RegionalRepository repository;
    private final RegionalIntegrationService integrationService;

    public RegionalService(RegionalRepository repository, RegionalIntegrationService integrationService) {
        this.repository = repository;
        this.integrationService = integrationService;
    }

    @Transactional
    public String sincronizarRegionais() {
        List<RegionalExternaDto> externas = integrationService.buscarRegionaisExternas();

        List<Regional> locaisAtivas = repository.findByAtivoTrue();

        Map<Integer, Regional> mapaLocais = locaisAtivas.stream()
                .collect(Collectors.toMap(Regional::getIdExterno, Function.identity()));

        int criados = 0;
        int atualizados = 0;
        int inativados = 0;

        for (RegionalExternaDto externa : externas) {
            Regional local = mapaLocais.get(externa.id());

            if (local == null) {
                repository.save(new Regional(externa.id(), externa.nome()));
                criados++;
            } else {
                mapaLocais.remove(externa.id());

                if (!local.getNome().equals(externa.nome())) {
                    local.setAtivo(false);
                    repository.save(local);

                    repository.save(new Regional(externa.id(), externa.nome()));
                    atualizados++;
                }
            }
        }

        for (Regional sobras : mapaLocais.values()) {
            sobras.setAtivo(false);
            repository.save(sobras);
            inativados++;
        }

        return String.format("Sincronização concluída. Criados: %d, Atualizados: %d, Inativados: %d",
                criados, atualizados, inativados);
    }

    public List<Regional> listarTodas() {
        return repository.findAll();
    }
}