package org.example.construconectaapinosql.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.example.construconectaapinosql.model.Administrador;
import org.example.construconectaapinosql.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdministradorController {
    private final AdministradorService administradorService;
    private final Validator validator;

    @Autowired
    public AdministradorController(
            AdministradorService administradorService,
            Validator validator
    ) {
        this.administradorService = administradorService;
        this.validator = validator;
    }

    @GetMapping("/admins")
    @Operation(summary = "Show all admins", description = "Returns a list of all available admins")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public List<Administrador> getAdmins() {
        return administradorService.findAllAdmins();
    }

    @PostMapping("/add")
    @Operation(summary = "Add a new admin", description = "Create a new admin and saves it to the database")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "201", description = "Admin created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or admin already exists",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "Data integrity violation",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> addAdmin(@Valid @RequestBody Administrador admin,
                                      BindingResult result
    ) {
        if (result.hasErrors()) {
            StringBuilder sb = new StringBuilder("Erros de validação:\n ");
            result.getAllErrors().forEach(error -> {
                sb.append(" |\n|");
                sb.append(error.getDefaultMessage());
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(sb.toString());
        }

        try {
            Administrador savedAdm = administradorService.saveAdmins(admin);
            if (savedAdm != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(savedAdm);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Administrador já existe.");
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar admin: \n" + e.getMessage());
        }
    }

    @PatchMapping("/update/{adminId}")
    @Operation(summary = "Update a admin", description = "Updates the admin data with the specified adminId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateAdminById(@PathVariable String adminId,
                                             @RequestBody Map<String, Object> updates) {
        try {
            ObjectId id = new ObjectId(adminId);
            Administrador adm = administradorService.findAdminsById(id);

            // Lista de campos válidos que podem ser atualizados
            List<String> validFields = Arrays.asList("usuario", "email", "senha");

            // Itera sobre as atualizações e só aplica as que são válidas
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                if (!validFields.contains(field)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo '" + field + "' não é válido para atualização.");
                }

                switch (field) {
                    case "usuario":
                        adm.setUsuario((String) entry.getValue());
                        break;
                    case "email":
                        adm.setEmail((String) entry.getValue());
                        break;
                    case "senha":
                        adm.setSenha((String) entry.getValue());
                        break;
                    default:
                        break;
                }
            }

            // Validação do admin atualizado
            DataBinder binder = new DataBinder(adm);
            binder.setValidator(validator);
            binder.validate();
            BindingResult result = binder.getBindingResult();
            if (result.hasErrors()) {
                Map<String, String> errors = validate(result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            administradorService.saveAdmins(adm);
            return ResponseEntity.ok("O cupom de desconto com adminId " + adminId + " foi atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para ObjectId: " + adminId);
        } catch (DataIntegrityViolationException e) {
            // Identifica qual campo violou a restrição UNIQUE
            String message = e.getRootCause().getMessage();
            if (message.contains("usuario")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: Usuário já está em uso.");
            } else if (message.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: E-mail já está em uso.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: " + e.getMessage());
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado: " + e.getMessage());
        }
    }

    @PatchMapping("/update/{userAdm}")
    @Operation(summary = "Update a admin", description = "Updates the admin data with the specified userAdm")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateAdminByUser(@PathVariable String userAdm,
                                               @RequestBody Map<String, Object> updates) {
        try {
            List<Administrador> admim = administradorService.findByUsuarioLikeIgnoreCase(userAdm);

            if (admim.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado.");
            }

            Administrador adm = getAdmins().get(0);

            // Lista de campos válidos que podem ser atualizados
            List<String> validFields = Arrays.asList("usuario", "email", "senha");

            // Itera sobre as atualizações e só aplica as que são válidas
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                if (!validFields.contains(field)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo '" + field + "' não é válido para atualização.");
                }

                switch (field) {
                    case "usuario":
                        adm.setUsuario((String) entry.getValue());
                        break;
                    case "email":
                        adm.setEmail((String) entry.getValue());
                        break;
                    case "senha":
                        adm.setSenha((String) entry.getValue());
                        break;
                    default:
                        break;
                }
            }

            // Validação do admin atualizado
            DataBinder binder = new DataBinder(adm);
            binder.setValidator(validator);
            binder.validate();
            BindingResult result = binder.getBindingResult();
            if (result.hasErrors()) {
                Map<String, String> errors = validate(result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            administradorService.saveAdmins(adm);
            return ResponseEntity.ok("O cupom de desconto com usuario " + userAdm + " foi atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para ObjectId: " + userAdm);
        } catch (DataIntegrityViolationException e) {
            // Identifica qual campo violou a restrição UNIQUE
            String message = e.getRootCause().getMessage();
            if (message.contains("usuario")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: Usuário já está em uso.");
            } else if (message.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: E-mail já está em uso.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: " + e.getMessage());
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado: " + e.getMessage());
        }
    }

    @PatchMapping("/update/{email}")
    @Operation(summary = "Update a admin", description = "Updates the admin data with the specified email")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateAdminByEmail(@PathVariable String email,
                                                @RequestBody Map<String, Object> updates) {
        try {
            List<Administrador> admim = administradorService.findByEmailLikeIgnoreCase(email);

            if (admim.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado.");
            }

            Administrador adm = getAdmins().get(0);

            // Lista de campos válidos que podem ser atualizados
            List<String> validFields = Arrays.asList("usuario", "email", "senha");

            // Itera sobre as atualizações e só aplica as que são válidas
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                if (!validFields.contains(field)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo '" + field + "' não é válido para atualização.");
                }

                switch (field) {
                    case "usuario":
                        adm.setUsuario((String) entry.getValue());
                        break;
                    case "email":
                        adm.setEmail((String) entry.getValue());
                        break;
                    case "senha":
                        adm.setSenha((String) entry.getValue());
                        break;
                    default:
                        break;
                }
            }

            // Validação do admin atualizado
            DataBinder binder = new DataBinder(adm);
            binder.setValidator(validator);
            binder.validate();
            BindingResult result = binder.getBindingResult();
            if (result.hasErrors()) {
                Map<String, String> errors = validate(result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            administradorService.saveAdmins(adm);
            return ResponseEntity.ok("O cupom de desconto com e-mail " + email + " foi atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para ObjectId: " + email);
        } catch (DataIntegrityViolationException e) {
            // Identifica qual campo violou a restrição UNIQUE
            String message = e.getRootCause().getMessage();
            if (message.contains("usuario")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: Usuário já está em uso.");
            } else if (message.contains("email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: E-mail já está em uso.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: " + e.getMessage());
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{adminId}")
    @Operation(summary = "Delete a admin", description = "Deletes the admin with the specified adminId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> deleteAdminByAdminId(@PathVariable String adminId) {
        try {
            ObjectId id = new ObjectId(adminId);
            administradorService.deleteAdminsById(id);
            return ResponseEntity.ok("Administrador excluído com sucesso");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar admin: \n" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{email}")
    @Operation(summary = "Delete a admin", description = "Deletes the admin with the specified email")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> deleteAdminByAdminEmail(@PathVariable String email) {
        try {
            administradorService.deleteAdminsByEmail(email);
            return ResponseEntity.ok("Administrador excluído com sucesso");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar admin: \n" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{userAdm}")
    @Operation(summary = "Delete a admin", description = "Deletes the admin with the specified userAdm")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> deleteAdminByAdminUserAdmin(@PathVariable String userAdm) {
        try {
            administradorService.deleteAdminsByUser(userAdm);
            return ResponseEntity.ok("Administrador excluído com sucesso");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar admin: \n" + e.getMessage());
        }
    }

    @GetMapping("/findByUserAdmin/{userAdm}")
    @Operation(summary = "Find admin by userAdm", description = "Returns the admin with the specified userAdm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> searchByUserAdm(@PathVariable String userAdm) {
        List<Administrador> lAdmin = administradorService.findByUsuarioLikeIgnoreCase(userAdm);
        if (!lAdmin.isEmpty()) {
            return ResponseEntity.ok(lAdmin);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado.");
        }
    }

    @GetMapping("/findByEmailAdmin/{email}")
    @Operation(summary = "Find admin by email", description = "Returns the admin with the specified email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Administrador.class))),
            @ApiResponse(responseCode = "404", description = "Admin not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> searchByEmailAdm(@PathVariable String email) {
        List<Administrador> lAdmin = administradorService.findByEmailLikeIgnoreCase(email);
        if (!lAdmin.isEmpty()) {
            return ResponseEntity.ok(lAdmin);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador não encontrado.");
        }
    }

    public Map<String, String> validate(BindingResult resultado) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : resultado.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

}
