package org.example.construconectaapinosql.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class Desconto {
    @Id
    @Field("_id")
    @Schema(description = "ObjectId do Cupom de desconto", example = "5f6b5f7b5f6b5f6b5f6b5f6b")
    private ObjectId id;

    @Field
    @NotBlank(message = "Cupom de desconto deve ser informado")
    @Schema(description = "Cupom de desconto", example = "VAIDE10")
    private String cupom;

    @Field("valor_desconto")
    @NotNull(message = "Valor do desconto deve ser informado")
    @Schema(description = "Valor do desconto", example = "10.0")
    private Double valorDesconto;

    public Desconto() {
    }

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getCupom() {
        return cupom;
    }

    public void setCupom(String cupom) {
        this.cupom = cupom;
    }

    public Double getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(Double valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    @Override
    public String toString() {
        return "Desconto{" +
                "id='" + id + '\'' +
                ", cupom='" + cupom + '\'' +
                ", valorDesconto='" + valorDesconto + '\'' +
                '}';
    }
}
