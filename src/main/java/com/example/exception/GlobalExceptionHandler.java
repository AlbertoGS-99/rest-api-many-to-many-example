package com.example.exception;

import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.spring_security_jwt.payload.response.MessageResponse;
import com.example.spring_security_jwt.payload.response.ValidationErrorsResponse;
import com.example.utilities.ValidationErrorsUtil;

/**
 * Manejador global de excepciones (Controller Advice) que se encarga de
 * responder con un JSON que contenga TODOS los errores encontrados en la
 * peticion (request) recibida, cuando dicha peticion esta mal formada
 *
 * Concretamente:
 *
 * 1- Cuando el cuerpo (body) de la peticion no es un JSON valido, es decir,
 * cuando esta mal formado (sintaxis incorrecta, por ejemplo: una coma de mas,
 * una llave sin cerrar, etc.) y se lanza HttpMessageNotReadableException
 *
 * 2- Cuando el JSON es valido sintacticamente pero no cumple las restricciones
 * de validacion declaradas en el objeto que recibe el endpoint, y se lanza
 * MethodArgumentNotValidException / BindException
 *
 * 3- Cuando durante el registro (signup) de un usuario ya existe el username o
 * el email suministrado
 *
 * La anotacion @Order con la precedencia mas alta garantiza que sus
 * manejadores tienen prioridad frente a cualquier otro ControllerAdvice, como
 * por ejemplo el que gestiona los recursos no encontrados (404)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * El JSON recibido en el cuerpo (body) de la peticion esta mal formado, es
	 * decir, que no se ha podido convertir (deserializar) en el objeto que espera
	 * recibir el endpoint
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ValidationErrorsResponse> handleMalformedJson(
			HttpMessageNotReadableException exception) {

		String causeMessage = exception.getMostSpecificCause() != null
				? exception.getMostSpecificCause().getMessage()
				: exception.getMessage();

		Map<String, List<String>> errors = Map.of("body", List.of(causeMessage));

		ValidationErrorsResponse response = ValidationErrorsResponse.builder()
				.message("Error: el JSON recibido en la peticion esta mal formado")
				.errors(errors)
				.build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * El JSON esta bien formado, pero el objeto recibido no supera la validacion
	 * declarada con las annotations de Jakarta Validation (@NotBlank, @Email,
	 * @Size, etc.), y se devuelven TODOS los errores encontrados
	 *
	 * Conviene saber que MethodArgumentNotValidException (que es la excepcion que
	 * se lanza cuando el endpoint recibe @Valid junto a un BindingResult) hereda
	 * de BindException, por lo que este manejador la cubre a ella y a cualquier
	 * otra excepcion de enlazado (binding) de la peticion
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ValidationErrorsResponse> handleBindException(BindException exception) {

		Map<String, List<String>> errors = ValidationErrorsUtil.getAllErrors(exception.getBindingResult());

		ValidationErrorsResponse response = ValidationErrorsResponse.builder()
				.message("Error: el JSON recibido en la peticion no es valido")
				.errors(errors)
				.build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Durante el registro (signup) el username suministrado ya esta en uso por
	 * otro usuario
	 */
	@ExceptionHandler(UsernameAlreadyTakenException.class)
	public ResponseEntity<MessageResponse> handleUsernameAlreadyTaken(
			UsernameAlreadyTakenException exception) {

		return new ResponseEntity<>(new MessageResponse(exception.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	/**
	 * Durante el registro (signup) el email suministrado ya esta en uso por
	 * otro usuario
	 */
	@ExceptionHandler(EmailAlreadyInUseException.class)
	public ResponseEntity<MessageResponse> handleEmailAlreadyInUse(
			EmailAlreadyInUseException exception) {

		return new ResponseEntity<>(new MessageResponse(exception.getMessage()),
				HttpStatus.BAD_REQUEST);
	}
}
