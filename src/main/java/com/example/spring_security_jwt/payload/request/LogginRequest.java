package com.example.spring_security_jwt.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LogginRequest {

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email tiene que tener un formato valido")
	private String email;

	@NotBlank(message = "El password es obligatorio")
	private String password;
}
