package com.example.exception;

/**
 * Excepcion que se lanza cuando en el registro (signup) de un usuario ya existe
 * el email suministrado
 */
public class EmailAlreadyInUseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailAlreadyInUseException(String message) {
		super(message);
	}
}
