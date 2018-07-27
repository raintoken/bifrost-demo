package com.raincheck.token.demoapp.dto;

import javax.validation.constraints.NotNull;

public class LoadAccountDto {

	@NotNull
	private String seed;

	@NotNull
	private String name;

	public String getSeed() {
		return seed;
	}

	public String getName() {
		return name;
	}
}
