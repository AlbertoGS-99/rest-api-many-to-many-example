package com.example.spring_security_jwt.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Componente que se encarga de responder con un 401 (Unauthorized) cuando la
 * peticion (request) no lleva credenciales validas, es decir, cuando no se ha
 * enviado ningun token JWT o el token enviado esta ausente, caducado o es
 * invalido
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthEntryPointJwt.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

		LOGGER.error("Unauthorized error: {} ", authException.getMessage());

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
	}
}
