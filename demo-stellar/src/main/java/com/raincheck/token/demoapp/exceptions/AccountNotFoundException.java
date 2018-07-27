package com.raincheck.token.demoapp.exceptions;

public class AccountNotFoundException extends RuntimeException {

	public AccountNotFoundException(final String msg) {
		super(msg);
	}

}
