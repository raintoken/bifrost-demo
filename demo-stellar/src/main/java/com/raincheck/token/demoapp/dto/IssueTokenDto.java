package com.raincheck.token.demoapp.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class IssueTokenDto {

	@Size(min = 1, max = 4)
	@NotNull
	private String code;

	@NotNull
	private String issuerAccountId;

	@NotNull
	private String distributorAccountId;

	@NotNull
	@Positive
	private Long amount;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIssuerAccountId() {
		return issuerAccountId;
	}

	public void setIssuerAccountId(String issuerAccountId) {
		this.issuerAccountId = issuerAccountId;
	}

	public String getDistributorAccountId() {
		return distributorAccountId;
	}

	public void setDistributorAccountId(String distributorAccountId) {
		this.distributorAccountId = distributorAccountId;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("IssueTokenDto{");
		sb.append("code='").append(code).append('\'');
		sb.append(", issuerAccountId='").append(issuerAccountId).append('\'');
		sb.append(", distributorAccountId='").append(distributorAccountId).append('\'');
		sb.append(", amount='").append(amount).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
