package com.example.utilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Utilidad que recupera TODOS los errores de validacion que tiene un objeto
 * recibido en el cuerpo (body) de una peticion (request), para poder devolverlos
 * todos juntos en el JSON de respuesta, y no solamente el primero que se
 * encuentre
 */
public final class ValidationErrorsUtil {

	private ValidationErrorsUtil() {
	}

	public static Map<String, List<String>> getAllErrors(BindingResult bindingResult) {

		Map<String, List<String>> errors = new LinkedHashMap<>();

		// Errores de campos concretos del JSON recibido (por ejemplo: username, email)
		bindingResult.getFieldErrors().forEach(fieldError -> errors
				.computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
				.add(fieldError.getDefaultMessage()));

		// Errores globales del objeto recibido (por ejemplo, los de la anotacion
		// @Valid a nivel de clase)
		bindingResult.getGlobalErrors()
				.forEach((ObjectError objectError) -> errors
						.computeIfAbsent(objectError.getObjectName(), key -> new ArrayList<>())
						.add(objectError.getDefaultMessage()));

		return errors;
	}
}
