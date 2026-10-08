package com.example.exception;

/**
 * Excepcion que se lanza cuando en el registro (signup) de un usuario ya existe
 * el username suministrado
 */
public class UsernameAlreadyTakenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsernameAlreadyTakenException(String message) {
		super(message);
	}
}
