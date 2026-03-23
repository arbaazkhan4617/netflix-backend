package com.netflix.backend.exception;

public class AccountNotVerifiedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AccountNotVerifiedException(String message) {
		super(message);
	}
}