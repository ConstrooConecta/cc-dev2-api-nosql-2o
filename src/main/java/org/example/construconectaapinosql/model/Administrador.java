package org.example.construconectaapinosql.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class Administrador {
    @Id
    @Field("_id")
    @Schema(description = "Id do administrador", example = "5f6b5f7b5f6b5f6b5f6b5f6b")
    private ObjectId id;

    @Field
    @Schema(description = "Usuario administrador", example = "Controo")
    private String usuario;

    @Field
    @Schema(description = "Email do administrador", example = "admin@admin.admin")
    private String email;

    @Field
    @Schema(description = "Senha do administrador", example = "admin123")
    private String senha;

    public Administrador() {
    }

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "id=" + id +
                ", usuario='" + usuario + '\'' +
                ", email='" + email + '\'' +
                ", senha='" + senha + '\'' +
                '}';
    }
}
