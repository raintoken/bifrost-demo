package com.raincheck.token.demoapp.dto;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class CreateTokenDto {

	@NotNull
	@Size(min = 1, max = 4)
	private String assetCode;

	@NotNull
	private String issuerId;

	@NotNull
	@Positive
	private Long amount;

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public String getAssetCode() {
		return assetCode;
	}

	public void setAssetCode(String assetCode) {
		this.assetCode = assetCode;
	}

	public String getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(String issuerId) {
		this.issuerId = issuerId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CreateTokenDto dto = (CreateTokenDto) o;
		return Objects.equals(assetCode, dto.assetCode) &&
				Objects.equals(issuerId, dto.issuerId) &&
				Objects.equals(amount, dto.amount);
	}

	@Override
	public int hashCode() {

		return Objects.hash(assetCode, issuerId, amount);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CreateTokenDto{");
		sb.append("assetCode='").append(assetCode).append('\'');
		sb.append(", issuerId='").append(issuerId).append('\'');
		sb.append(", amount=").append(amount);
		sb.append('}');
		return sb.toString();
	}
}
