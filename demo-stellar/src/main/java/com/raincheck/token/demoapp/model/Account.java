package com.raincheck.token.demoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.UUID;
import org.stellar.sdk.KeyPair;

public class Account {

	private String id;
	private String name;
	private String stellarAccountId;
	private char[] secretSeed;

	public Account() {
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStellarAccountId() {
		return stellarAccountId;
	}

	public void setStellarAccountId(String stellarAccountId) {
		this.stellarAccountId = stellarAccountId;
	}

	@JsonIgnore
	public KeyPair getKeyPair() {
		return KeyPair.fromSecretSeed(this.secretSeed);
	}

	public char[] getSecretSeed() {
		return secretSeed;
	}

	public void setSecretSeed(char[] secretSeed) {
		this.secretSeed = secretSeed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Account account = (Account) o;
		return Objects.equals(id, account.id) &&
				Objects.equals(name, account.name) &&
				Objects.equals(stellarAccountId, account.stellarAccountId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, stellarAccountId);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Account{");
		sb.append("id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append(", stellarAccountId='").append(stellarAccountId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
