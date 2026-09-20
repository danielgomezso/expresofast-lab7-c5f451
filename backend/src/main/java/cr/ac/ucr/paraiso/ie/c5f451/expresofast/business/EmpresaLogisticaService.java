package cr.ac.ucr.paraiso.ie.c5f451.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5f451.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5f451.expresofast.exception.*;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaLogisticaService {
    private final EmpresaLogisticaRepository repository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmpresaLogistica registrarEmpresa(EmpresaLogistica empresa) {
        if (empresa == null || empresa.getNombre() == null || empresa.getNombre().isBlank() ||
                empresa.getCedulaJuridica() == null || empresa.getCedulaJuridica().isBlank())
            throw new IllegalArgumentException("Nombre y cédula jurídica son obligatorios.");
        if (repository.existsByCedulaJuridica(empresa.getCedulaJuridica())
                || repository.existsByNombre(empresa.getNombre()))
            throw new DuplicateResourceException("La empresa ya existe.");
        empresa.setFechaRegistro(LocalDateTime.now());
        return repository.save(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaLogistica obtenerEmpresa(@NonNull Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));
    }

    @Transactional(readOnly = true)
    public List<EmpresaLogistica> listarEmpresas() {
        return repository.findAll();
    }
}
