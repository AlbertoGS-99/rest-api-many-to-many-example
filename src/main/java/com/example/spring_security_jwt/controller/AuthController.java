package com.example.spring_security_jwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_security_jwt.payload.request.LogginRequest;
import com.example.spring_security_jwt.payload.request.SignupRequest;
import com.example.spring_security_jwt.payload.response.JwtResponse;
import com.example.spring_security_jwt.payload.response.MessageResponse;
import com.example.spring_security_jwt.payload.response.ValidationErrorsResponse;
import com.example.services.AuthService;
import com.example.utilities.ValidationErrorsUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * Endpoint de registro (signup) de un usuario
	 *
	 * Primero se comprueba si el JSON recibido esta bien formado y cumple todas
	 * las restricciones de validacion declaradas en SignupRequest, y en tal caso
	 * se devuelven TODOS los errores encontrados en la peticion (request), y no
	 * solamente el primero
	 */
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest,
			BindingResult validationResults) {

		if (validationResults.hasErrors()) {

			ValidationErrorsResponse validationErrorsResponse = ValidationErrorsResponse.builder()
					.message("Error: el JSON recibido en la peticion de registro no es valido")
					.errors(ValidationErrorsUtil.getAllErrors(validationResults))
					.build();

			return ResponseEntity.badRequest().body(validationErrorsResponse);
		}

		MessageResponse messageResponse = authService.register(signupRequest);

		return ResponseEntity.ok(messageResponse);
	}

	/**
	 * Endpoint que permite logearse a un usuario que se ha registrado
	 * previamente, suministrando su email (y no su username) y su password
	 */
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LogginRequest logginRequest,
			BindingResult validationResults) {

		/**
		 * Si el JSON recibido esta mal formado o no cumple las restricciones de
		 * validacion, se devuelven TODOS los errores encontrados en la peticion
		 * (request)
		 */
		if (validationResults.hasErrors()) {

			ValidationErrorsResponse validationErrorsResponse = ValidationErrorsResponse.builder()
					.message("Error: el JSON recibido en la peticion de login no es valido")
					.errors(ValidationErrorsUtil.getAllErrors(validationResults))
					.build();

			return ResponseEntity.badRequest().body(validationErrorsResponse);
		}

		try {

			JwtResponse jwtResponse = authService.login(logginRequest);

			return ResponseEntity.ok(jwtResponse);

		} catch (AuthenticationException e) {

			// El email suministrado no esta registrado o el password no coincide
			return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
					.body(new MessageResponse("Error: el email o el password son incorrectos"));
		}
	}
}
