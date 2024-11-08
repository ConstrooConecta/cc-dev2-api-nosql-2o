package org.example.construconectaapinosql.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.example.construconectaapinosql.model.Desconto;
import org.example.construconectaapinosql.service.DescontoService;
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
@RequestMapping("/discounts")
public class DescontoController {
    private final DescontoService descontoService;
    private final Validator validator;

    @Autowired
    public DescontoController(
            DescontoService descontoService,
            Validator validator
    ) {
        this.descontoService = descontoService;
        this.validator = validator;
    }

    @GetMapping("/discounts")
    @Operation(summary = "Show all vouchers", description = "Returns a list of all available vouchers")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public List<Desconto> getDiscounts() {
        return descontoService.findAllVouchers();
    }

    @PostMapping("/add")
    @Operation(summary = "Add a new voucher", description = "Create a new voucher and saves it to the database")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Voucher created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "201", description = "Voucher created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or voucher already exists",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "Data integrity violation",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> addVoucher(@Valid @RequestBody Desconto voucher,
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
            Desconto savedVoucher = descontoService.saveVouchers(voucher);
            if (savedVoucher != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(savedVoucher);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cupom de desconto já existe.");
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar cupom: \n" + e.getMessage());
        }
    }

    @PatchMapping("/update/{voucherId}")
    @Operation(summary = "Update a voucher", description = "Updates the voucher data with the specified voucherId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Voucher updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateVoucherById(@PathVariable String voucherId,
                                               @RequestBody Map<String, Object> updates) {
        try {
            ObjectId id = new ObjectId(voucherId);
            Desconto voucher = descontoService.findVouchersById(id);

            // Lista de campos válidos que podem ser atualizados
            List<String> validFields = Arrays.asList("cupom", "valor_desconto");

            // Itera sobre as atualizações e só aplica as que são válidas
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                if (!validFields.contains(field)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo '" + field + "' não é válido para atualização.");
                }

                switch (field) {
                    case "cupom":
                        voucher.setCupom((String) entry.getValue());
                        break;
                    case "valorDesconto":
                        // Verifica se o valor é Integer e converte para Double se necessário
                        Object value = entry.getValue();
                        if (value instanceof Integer) {
                            voucher.setValorDesconto(((Integer) value).doubleValue());
                        } else if (value instanceof Double) {
                            voucher.setValorDesconto((Double) value);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para o campo 'valorDesconto'. Deve ser um número.");
                        }
                        break;
                    default:
                        break;
                }
            }

            // Validação do voucher atualizado
            DataBinder binder = new DataBinder(voucher);
            binder.setValidator(validator);
            binder.validate();
            BindingResult result = binder.getBindingResult();
            if (result.hasErrors()) {
                Map<String, String> errors = validate(result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }

            descontoService.saveVouchers(voucher);
            return ResponseEntity.ok("O cupom de desconto com voucherId " + voucherId + " foi atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para ObjectId: " + voucherId);
        } catch (DataIntegrityViolationException e) {
            // Identifica qual campo violou a restrição UNIQUE
            String message = e.getRootCause().getMessage();
            if (message.contains("cupom")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: Cupom já está em uso.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: " + e.getMessage());
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher não encontrado: " + e.getMessage());
        }
    }

    @PatchMapping("/updateByCupom/{cupom}")
    @Operation(summary = "Update a voucher by coupon name", description = "Updates voucher data using the specified coupon name")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Voucher updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Voucher not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateVoucherByCupom(@PathVariable String cupom, @RequestBody Map<String, Object> updates) {
        try {
            List<Desconto> vouchers = descontoService.findByVoucherName(cupom);

            if (vouchers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cupom não encontrado.");
            }

            Desconto voucher = vouchers.get(0);

            // Lista de campos válidos que podem ser atualizados
            List<String> validFields = Arrays.asList("cupom", "valorDesconto");

            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                if (!validFields.contains(field)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo '" + field + "' não é válido para atualização.");
                }

                switch (field) {
                    case "cupom":
                        voucher.setCupom((String) entry.getValue());
                        break;
                    case "valorDesconto":
                        Object value = entry.getValue();
                        if (value instanceof Integer) {
                            voucher.setValorDesconto(((Integer) value).doubleValue());
                        } else if (value instanceof Double) {
                            voucher.setValorDesconto((Double) value);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para o campo 'valorDesconto'. Deve ser um número.");
                        }
                        break;
                    default:
                        break;
                }
            }

            descontoService.saveVouchers(voucher);
            return ResponseEntity.ok("O cupom de desconto '" + cupom + "' foi atualizado com sucesso.");
        } catch (DataIntegrityViolationException e) {
            String message = e.getRootCause().getMessage();
            if (message.contains("cupom")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: Cupom já está em uso.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: " + e.getMessage());
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar cupom: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{voucherId}")
    @Operation(summary = "Delete a voucher", description = "Deletes the voucher with the specified voucherId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Voucher deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> deleteVoucherByVoucherId(@PathVariable String voucherId) {
        try {
            ObjectId id = new ObjectId(voucherId);
            descontoService.deleteVoucher(id);
            return ResponseEntity.ok("Cupom de desconto excluído com sucesso");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar cupom: \n" + e.getMessage());
        }
    }

    @DeleteMapping("/deleteByVoucherName/{voucherName}")
    @Operation(summary = "Delete a voucher by voucher name", description = "Deletes the voucher with the specified voucher name")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Voucher deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> deleteByVoucherName(@PathVariable String voucherName) {
        try {
            descontoService.deleteVoucherByVoucherName(voucherName);
            return ResponseEntity.ok("Cupom de desconto excluído com sucesso");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro de integridade de dados: \n" + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao acessar o banco de dados: \n" + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar cupom de desconto: \n" + e.getMessage());
        }
    }

    @GetMapping("/findById/{voucherId}")
    @Operation(summary = "Find voucher by voucherId", description = "Returns the voucher with the specified voucherId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Voucher found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> findVoucherById(@Parameter @PathVariable String voucherId) {
        try {
            ObjectId id = new ObjectId(voucherId);
            return ResponseEntity.ok(descontoService.findVouchersById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato inválido para ObjectId: " + voucherId);
        }
    }

    @GetMapping("/findByVoucherName/{voucherName}")
    @Operation(summary = "Find voucher by voucherName", description = "Returns the voucher with the specified voucherName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Voucher found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> searchByVoucherName(@PathVariable String voucherName) {
        List<Desconto> lVoucher = descontoService.findByVoucherName(voucherName);
        if (!lVoucher.isEmpty()) {
            return ResponseEntity.ok(lVoucher);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cupom de desconto não encontrado.");
        }
    }

    @GetMapping("/discounts/over10")
    @Operation(summary = "Retrieve discounts with percentage over 10%", description = "Returns a list of discounts where percentage is greater than 10, with percentage in decimal format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = Desconto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getDiscountsWithPercentageOver10() {
        try {
            List<Desconto> descontos = descontoService.findDiscountsWithPercentageOver10();
            return ResponseEntity.ok(descontos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar cupons de desconto: " + e.getMessage());
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
