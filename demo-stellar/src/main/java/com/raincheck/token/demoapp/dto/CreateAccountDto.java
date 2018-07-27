package com.raincheck.token.demoapp.dto;

import javax.validation.constraints.NotNull;

public class CreateAccountDto {

	@NotNull
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
