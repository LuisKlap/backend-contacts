package com.uex.contacts.dto.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
                @NotBlank(message = "Nome é obrigatório") @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres") String name,

                @NotBlank(message = "CPF é obrigatório") @Size(min = 11, max = 14, message = "CPF inválido") String cpf,

                @NotBlank(message = "Telefone é obrigatório") @Size(max = 30, message = "Telefone muito longo") String phone,

                @NotBlank(message = "CEP é obrigatório") @Size(min = 8, max = 9, message = "CEP inválido") String cep,

                @NotBlank(message = "UF é obrigatório") @Size(min = 2, max = 2, message = "UF inválida") String state,

                @NotBlank(message = "Cidade é obrigatória") @Size(max = 100, message = "Cidade muito longa") String city,

                @NotBlank(message = "Logradouro (rua) é obrigatório") @Size(max = 200, message = "Logradouro muito longo") String street,

                @NotBlank(message = "Bairro é obrigatório") @Size(max = 200, message = "Logradouro muito longo") String neighborhood,

                @NotBlank(message = "Número é obrigatório") @Size(max = 20, message = "Número muito longo") String number,

                @Size(max = 100, message = "Complemento muito longo") String complement) {
}
