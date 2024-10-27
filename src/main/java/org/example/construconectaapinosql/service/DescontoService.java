package org.example.construconectaapinosql.service;

import org.bson.types.ObjectId;
import org.example.construconectaapinosql.model.Desconto;
import org.example.construconectaapinosql.repository.DescontoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DescontoService {
    private final DescontoRepository descontoRepository;
    private final MongoTemplate mongoTemplate;

    public DescontoService(
            DescontoRepository descontoRepository,
            MongoTemplate mongoTemplate
    ) {
        this.descontoRepository = descontoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Desconto> findAllVouchers() {
        return descontoRepository.findAll();
    }

    @Transactional
    public Desconto saveVouchers(Desconto voucher) {
        boolean isUpdate = voucher.getId() != null && descontoRepository.existsById(new ObjectId(voucher.getId()));
        validateUniqueFields(voucher, isUpdate); // Validação de campos únicos, passando o estado de update
        return descontoRepository.save(voucher);
    }

    @Transactional
    public Desconto deleteVoucher(ObjectId id) {
        Desconto voucher = findVouchersById(id);
        descontoRepository.delete(voucher);
        return voucher;
    }

    @Transactional
    public void deleteVoucherByVoucherName(String voucher) {
        if (descontoRepository.findByCupomLikeIgnoreCase(voucher).isEmpty()) {
            throw new RuntimeException("Cupom de desconto não encontrado: [" + voucher + "]");
        }
        descontoRepository.deleteByCupom(voucher);
    }

    public Desconto findVouchersById(ObjectId id) {
        return descontoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupom de desconto não encontrado."));
    }

    public List<Desconto> findByVoucherName(String voucher) {
        return descontoRepository.findByCupomLikeIgnoreCase(voucher);
    }

    public List<Desconto> findDiscountsWithPercentageOver10() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("valor_desconto").gt(10.0)),
                Aggregation.project("cupom")
                        .andExpression("valor_desconto / 100").as("valor_desconto")
        );
        return mongoTemplate.aggregate(aggregation, "desconto", Desconto.class).getMappedResults();
    }

    private void validateUniqueFields(Desconto voucher, boolean isUpdate) {
        // Se não for uma atualização ou o CPF for diferente do CPF existente, validar
        if (!isUpdate || !descontoRepository.findById(voucher.getId()).get().getCupom().equals(voucher.getCupom())) {
            if (!descontoRepository.findByCupomLikeIgnoreCase(voucher.getCupom()).isEmpty()) {
                throw new RuntimeException("Cupom de desconto já existe.");
            }
        }
    }

}
