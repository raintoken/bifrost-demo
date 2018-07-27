package com.raincheck.token.demoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.UUID;
import org.stellar.sdk.Asset;

public class Token {

	private String id;
	private String code;
	private Long amount;
	private Account issuer;
	private Account distributor;
	private String price;

	public Token() {
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@JsonIgnore
	public Asset getAsset() {
		return Asset.createNonNativeAsset(this.code, this.issuer.getKeyPair());
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Account getIssuer() {
		return issuer;
	}

	public void setIssuer(Account issuer) {
		this.issuer = issuer;
	}

	public Account getDistributor() {
		return distributor;
	}

	public void setDistributor(Account distributor) {
		this.distributor = distributor;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Token token = (Token) o;
		return Objects.equals(id, token.id) &&
				Objects.equals(code, token.code) &&
				Objects.equals(amount, token.amount) &&
				Objects.equals(issuer, token.issuer) &&
				Objects.equals(distributor, token.distributor) &&
				Objects.equals(price, token.price);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, code, amount, issuer, distributor, price);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Token{");
		sb.append("id='").append(id).append('\'');
		sb.append(", code='").append(code).append('\'');
		sb.append(", amount=").append(amount);
		sb.append(", issuer=").append(issuer);
		sb.append(", distributor=").append(distributor);
		sb.append(", price=").append(price);
		sb.append('}');
		return sb.toString();
	}
}