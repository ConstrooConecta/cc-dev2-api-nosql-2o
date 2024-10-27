package org.example.construconectaapinosql.repository;

import org.bson.types.ObjectId;
import org.example.construconectaapinosql.model.Desconto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DescontoRepository extends MongoRepository<Desconto, ObjectId> {
    Optional<Desconto> findById(String id);

    List<Desconto> findByCupomLikeIgnoreCase(String cupom);

    void deleteByCupom(String cupom);
}
