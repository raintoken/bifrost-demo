package com.raincheck.token.demoapp.service;

import com.raincheck.token.demoapp.dto.ChangeTrustDto;
import com.raincheck.token.demoapp.dto.CreateAccountDto;
import com.raincheck.token.demoapp.dto.LoadAccountDto;
import com.raincheck.token.demoapp.dto.LockAccountDto;
import com.raincheck.token.demoapp.exceptions.AccountNotFoundException;
import com.raincheck.token.demoapp.exceptions.ChangeTrustException;
import com.raincheck.token.demoapp.exceptions.LockAccountException;
import com.raincheck.token.demoapp.model.Account;
import com.raincheck.token.demoapp.model.Token;
import com.raincheck.token.demoapp.model.TransactionResponse;
import com.raincheck.token.demoapp.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.stellar.sdk.KeyPair;

@Service
public class AccountService {

	private Map<String, Account> storage = new HashMap<>();

	private final StellarService stellarService;
	private final TokenService tokenService;

	public AccountService(final StellarService stellarService, TokenService tokenService) {
		this.stellarService = stellarService;
		this.tokenService = tokenService;
	}

	Account find(final String stellarAccountId) {
		return this.storage.get(stellarAccountId);
	}

	public Account create(final CreateAccountDto createAccountDto) {
		final Account account = new Account();
		account.setName(createAccountDto.getName());
		final KeyPair keyPair = this.stellarService.createAccount();
		account.setStellarAccountId(keyPair.getAccountId());
		account.setSecretSeed(keyPair.getSecretSeed());

		this.storage.put(account.getStellarAccountId(), account);
		return account;
	}

	public Account loadExistingAccount(final LoadAccountDto dto) {
		final KeyPair keyPair = this.stellarService.loadAccount(dto.getSeed());
		final Account account = new Account();
		account.setName(dto.getName());
		account.setStellarAccountId(keyPair.getAccountId());
		account.setSecretSeed(keyPair.getSecretSeed());

		this.storage.put(account.getStellarAccountId(), account);
		return account;
	}

	@Async
	public CompletableFuture<TransactionResponse> changeTrust(final ChangeTrustDto dto) {
		final String distributorId = dto.getDistributorId();
		if (!this.storage.containsKey(distributorId)) {
			throw new AccountNotFoundException(
					String.format("Distributor with id=%s not found", distributorId));
		}
		final String issuerId = dto.getIssuerId();

		if (!this.storage.containsKey(issuerId)) {
			throw new AccountNotFoundException(String.format("Issuer with id=%s not found", issuerId));
		}

		final Account distributorAccount = this.storage.get(distributorId);
		final Account issuerAccount = this.storage.get(issuerId);

		final Token token = this.tokenService
				.findByCodeAndIssuer(dto.getAssetCode(), issuerAccount.getStellarAccountId());
		token.setDistributor(distributorAccount);

		final String amount = String.valueOf(dto.getAmount());
		return this.stellarService.changeTrust(distributorAccount, token, amount)
				.thenApply(Util.txResponseBuilder)
				.exceptionally(throwable -> {
					token.setDistributor(null);
					throw new ChangeTrustException("Change trust operation failed.");
				});

	}

	@Async
	public CompletableFuture<TransactionResponse> lockAccount(final LockAccountDto dto) {
		final String accountId = dto.getAccountId();
		final Account account = this.storage.get(accountId);

		if (Objects.isNull(account)) {
			throw new AccountNotFoundException(String.format("Account %s not found.", accountId));
		}

		return this.stellarService.lockAccount(account)
				.thenApply(Util.txResponseBuilder)
				.exceptionally(throwable -> {
					throw new LockAccountException(throwable.getMessage());
				});
	}

}
