package com.raincheck.token.demoapp.dto;

import com.raincheck.token.demoapp.model.enums.DistributionState;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class TokenDistributionDto {

	@Size(min = 1, max = 4)
	@NotNull
	private String sellingAssetCode;

	@NotNull
	private String buyingAssetCode;

	@NotNull
	private String issuerAccountId;

	@NotNull
	private String price;

	@NotNull
	@Positive
	private Long amount;

	@NotNull
	private DistributionState distributionState;

	public String getSellingAssetCode() {
		return sellingAssetCode;
	}

	public void setSellingAssetCode(String sellingAssetCode) {
		this.sellingAssetCode = sellingAssetCode;
	}

	public String getBuyingAssetCode() {
		return buyingAssetCode;
	}

	public void setBuyingAssetCode(String buyingAssetCode) {
		this.buyingAssetCode = buyingAssetCode;
	}

	public String getIssuerAccountId() {
		return issuerAccountId;
	}

	public void setIssuerAccountId(String issuerAccountId) {
		this.issuerAccountId = issuerAccountId;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public DistributionState getDistributionState() {
		return distributionState;
	}

	public void setDistributionState(
			DistributionState distributionState) {
		this.distributionState = distributionState;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TokenDistributionDto dto = (TokenDistributionDto) o;
		return Objects.equals(sellingAssetCode, dto.sellingAssetCode) &&
				Objects.equals(buyingAssetCode, dto.buyingAssetCode) &&
				Objects.equals(issuerAccountId, dto.issuerAccountId) &&
				Objects.equals(price, dto.price) &&
				Objects.equals(amount, dto.amount) &&
				distributionState == dto.distributionState;
	}

	@Override
	public int hashCode() {
		return Objects
				.hash(sellingAssetCode, buyingAssetCode, issuerAccountId, price, amount, distributionState);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TokenDistributionDto{");
		sb.append("sellingAssetCode='").append(sellingAssetCode).append('\'');
		sb.append(", buyingAssetCode='").append(buyingAssetCode).append('\'');
		sb.append(", issuerAccountId='").append(issuerAccountId).append('\'');
		sb.append(", price=").append(price);
		sb.append(", amount=").append(amount);
		sb.append(", distributionState=").append(distributionState);
		sb.append('}');
		return sb.toString();
	}
}
