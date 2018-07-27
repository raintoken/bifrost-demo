package com.raincheck.token.demoapp.exceptions;

public class TokenNotFoundException extends RuntimeException {

	public TokenNotFoundException(final String msg) {
		super(msg);
	}

}
