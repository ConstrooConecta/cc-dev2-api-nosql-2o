package org.example.construconectaapinosql.service;

import org.bson.types.ObjectId;
import org.example.construconectaapinosql.model.Administrador;
import org.example.construconectaapinosql.repository.AdministradorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdministradorService {
    private final AdministradorRepository administradorRepository;

    public AdministradorService(
            AdministradorRepository administradorRepository
    ) {
        this.administradorRepository = administradorRepository;
    }

    public List<Administrador> findAllAdmins() {
        return administradorRepository.findAll();
    }

    @Transactional
    public Administrador saveAdmins(Administrador adm) {
        boolean isUpdate = adm.getId() != null && administradorRepository.existsById(new ObjectId(adm.getId()));
        validateUniqueFields(adm, isUpdate); // Validação de campos únicos, passando o estado de update
        return administradorRepository.save(adm);
    }

    @Transactional
    public Administrador deleteAdminsById(ObjectId id) {
        Administrador adm = findAdminsById(id);
        administradorRepository.deleteById(id);
        return adm;
    }

    @Transactional
    public void deleteAdminsByEmail(String email) {
        if (administradorRepository.findByEmailLikeIgnoreCase(email).isEmpty()) {
            throw new RuntimeException("Administrador nao encontrado.: [" + email + "]");
        }
        administradorRepository.deleteByEmail(email);
    }

    @Transactional
    public void deleteAdminsByUser(String user) {
        if (administradorRepository.findByUsuarioLikeIgnoreCase(user).isEmpty()) {
            throw new RuntimeException("Administrador nao encontrado.: [" + user + "]");
        }
        administradorRepository.deleteByEmail(user);
    }

    public Administrador findAdminsById(ObjectId id) {
        return administradorRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Administrador não encontrado."));
    }

    public List<Administrador> findByUsuarioLikeIgnoreCase(String usuario) {
        return administradorRepository.findByUsuarioLikeIgnoreCase(usuario);
    }

    public List<Administrador> findByEmailLikeIgnoreCase(String email) {
        return administradorRepository.findByEmailLikeIgnoreCase(email);
    }

    private void validateUniqueFields(Administrador adm, boolean isUpdate) {
        // Se não for uma atualização ou o CPF for diferente do CPF existente, validar
        if (!isUpdate || !administradorRepository.findById(adm.getId()).get().getUsuario().equals(adm.getUsuario())) {
            if (!administradorRepository.findByUsuarioLikeIgnoreCase(adm.getUsuario()).isEmpty()) {
                throw new RuntimeException("Este usuário já existe.");
            }
        }
        if (!isUpdate || !administradorRepository.findById(adm.getId()).get().getEmail().equals(adm.getEmail())) {
            if (!administradorRepository.findByUsuarioLikeIgnoreCase(adm.getEmail()).isEmpty()) {
                throw new RuntimeException("Este e-mail já existe.");
            }
        }
    }

}