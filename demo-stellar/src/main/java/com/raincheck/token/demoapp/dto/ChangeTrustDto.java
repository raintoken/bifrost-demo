package com.raincheck.token.demoapp.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class ChangeTrustDto {

	@NotNull
	private String distributorId;

	@NotNull
	private String issuerId;

	@Size(min = 1, max = 4)
	@NotNull
	private String assetCode;

	@NotNull
	@Positive
	private Long amount;

	public String getDistributorId() {
		return distributorId;
	}

	public void setDistributorId(String distributorId) {
		this.distributorId = distributorId;
	}

	public String getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(String issuerId) {
		this.issuerId = issuerId;
	}

	public String getAssetCode() {
		return assetCode;
	}

	public void setAssetCode(String assetCode) {
		this.assetCode = assetCode;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ChangeTrustDto{");
		sb.append("distributorId='").append(distributorId).append('\'');
		sb.append(", issuerId='").append(issuerId).append('\'');
		sb.append(", assetCode='").append(assetCode).append('\'');
		sb.append(", amount='").append(amount).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
