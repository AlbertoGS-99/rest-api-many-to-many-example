package com.example.services;

import com.example.spring_security_jwt.payload.request.LogginRequest;
import com.example.spring_security_jwt.payload.request.SignupRequest;
import com.example.spring_security_jwt.payload.response.JwtResponse;
import com.example.spring_security_jwt.payload.response.MessageResponse;

/**
 * Servicio de autenticacion que se encarga del registro (signup) y del login
 * (signin) de los usuarios registrados
 */
public interface AuthService {

	/**
	 * Registra un usuario nuevo con los roles suministrados en la peticion
	 * (request)
	 *
	 * @throws com.example.exception.UsernameAlreadyTakenException si el username
	 *                                                             ya esta en uso
	 * @throws com.example.exception.EmailAlreadyInUseException    si el email ya
	 *                                                             esta en uso
	 */
	MessageResponse register(SignupRequest signupRequest);

	/**
	 * Autentica a un usuario registrado utilizando su email (y no su username) y
	 * su password, y genera el token JWT correspondiente
	 *
	 * @throws org.springframework.security.core.AuthenticationException si el
	 *             email no esta registrado o el password no coincide
	 */
	JwtResponse login(LogginRequest logginRequest);
}
