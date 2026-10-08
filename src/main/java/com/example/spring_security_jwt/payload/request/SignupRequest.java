package com.example.spring_security_jwt.payload.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SignupRequest {

	@NotBlank(message = "El username es obligatorio")
	@Size(min = 3, max = 20, message = "El username tiene que tener entre 3 y 20 caracteres")
	private String username;

	@NotBlank(message = "El email es obligatorio")
	@Size(max = 45, message = "El email no puede superar los 45 caracteres")
	@Email(message = "El email tiene que tener un formato valido")
	private String email;

	private Set<String> role;

	@NotBlank(message = "El password es obligatorio")
	@Size(min = 6, max = 40, message = "El password tiene que tener entre 6 y 40 caracteres")
	private String password;
}
