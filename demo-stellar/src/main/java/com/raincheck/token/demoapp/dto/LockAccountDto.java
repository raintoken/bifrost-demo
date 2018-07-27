package com.raincheck.token.demoapp.dto;

import java.util.Objects;
import javax.validation.constraints.NotNull;

public class LockAccountDto {

	@NotNull
	private String accountId;

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LockAccountDto that = (LockAccountDto) o;
		return Objects.equals(accountId, that.accountId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("LockAccountDto{");
		sb.append("accountId='").append(accountId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
