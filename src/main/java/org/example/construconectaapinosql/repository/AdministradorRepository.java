package org.example.construconectaapinosql.repository;

import org.bson.types.ObjectId;
import org.example.construconectaapinosql.model.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends MongoRepository<Administrador, ObjectId> {
    Optional<Administrador> findById(String id);

    List<Administrador> findByUsuarioIgnoreCase(String usuario);

    List<Administrador> findByEmailIgnoreCase(String email);

    void deleteByUsuario(String usuario);

    void deleteByEmail(String email);
}
