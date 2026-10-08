package com.example.spring_security_jwt.payload.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta que se devuelve cuando la peticion (request) recibida no es valida,
 * es decir, cuando el JSON recibido esta mal formado o no cumple con las
 * restricciones de validacion (annotations) declaradas en el payload de la
 * peticion.
 *
 * Se devuelven TODOS los errores encontrados en el JSON recibido, y no
 * solamente el primero, para lo cual se utiliza un Map donde la key es el
 * campo del JSON y el value es la lista de mensajes de error de dicho campo
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ValidationErrorsResponse {

	private String message;

	private Map<String, List<String>> errors;
}
