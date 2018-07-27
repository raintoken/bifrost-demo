package com.raincheck.token.demoapp.model;

import java.util.Objects;

public class BalanceResponse {

	private String issuerId;
	private String targetId;
	private String assetCode;
	private String balance;

	public String getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(String issuerId) {
		this.issuerId = issuerId;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getAssetCode() {
		return assetCode;
	}

	public void setAssetCode(String assetCode) {
		this.assetCode = assetCode;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BalanceResponse that = (BalanceResponse) o;
		return Objects.equals(issuerId, that.issuerId) &&
				Objects.equals(targetId, that.targetId) &&
				Objects.equals(assetCode, that.assetCode) &&
				Objects.equals(balance, that.balance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(issuerId, targetId, assetCode, balance);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("BalanceResponse{");
		sb.append("issuerId='").append(issuerId).append('\'');
		sb.append(", targetId='").append(targetId).append('\'');
		sb.append(", assetCode='").append(assetCode).append('\'');
		sb.append(", balance=").append(balance);
		sb.append('}');
		return sb.toString();
	}
}
