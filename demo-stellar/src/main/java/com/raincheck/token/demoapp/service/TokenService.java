package com.raincheck.token.demoapp.service;

import com.raincheck.token.demoapp.dto.CreateTokenDto;
import com.raincheck.token.demoapp.dto.IssueTokenDto;
import com.raincheck.token.demoapp.dto.TokenDistributionDto;
import com.raincheck.token.demoapp.exceptions.TokenDistributionException;
import com.raincheck.token.demoapp.exceptions.TokenIssueException;
import com.raincheck.token.demoapp.exceptions.TokenNotFoundException;
import com.raincheck.token.demoapp.model.Account;
import com.raincheck.token.demoapp.model.BalanceResponse;
import com.raincheck.token.demoapp.model.Token;
import com.raincheck.token.demoapp.model.TransactionResponse;
import com.raincheck.token.demoapp.model.enums.DistributionState;
import com.raincheck.token.demoapp.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeNative;

@Service
public class TokenService {

	private Map<String, Token> tokenStorage = new HashMap<>();

	private final StellarService stellarService;
	private final AccountService accountService;

	public TokenService(StellarService stellarService, @Lazy AccountService accountService) {
		this.stellarService = stellarService;
		this.accountService = accountService;
	}

	public Token createToken(final CreateTokenDto dto) {
		final String issuerId = dto.getIssuerId();
		final Account issuer = this.accountService.find(issuerId);
		Asset.createNonNativeAsset(dto.getAssetCode(), issuer.getKeyPair());
		final Token token = new Token();
		token.setCode(dto.getAssetCode());
		token.setIssuer(issuer);
		token.setAmount(dto.getAmount());
		this.tokenStorage.put(token.getCode(), token);
		return token;
	}

	@Async
	public CompletableFuture<TransactionResponse> issueToken(final IssueTokenDto dto) {
		final Token token = findByCodeAndIssuer(dto.getCode(), dto.getIssuerAccountId());
		token.setAmount(dto.getAmount());
		return this.stellarService.issueToken(token)
				.thenApply(Util.txResponseBuilder)
				.exceptionally(throwable -> {
					throw new TokenIssueException(throwable.getMessage());
				});
	}

	@Async
	public CompletableFuture<TransactionResponse> manageTokenDistribution(final TokenDistributionDto dto) {
		final String issuerAccountId = dto.getIssuerAccountId();

		final Token sellingToken = findByCodeAndIssuer(dto.getSellingAssetCode(), issuerAccountId);
		final String buyingAssetCode = dto.getBuyingAssetCode();

		final Account issuer = sellingToken.getIssuer();
		final Asset sellingAsset = sellingToken.getAsset();
		final Asset buyingAsset;

		if ("XLM".equals(buyingAssetCode)) {
			buyingAsset = new AssetTypeNative();
		} else {
			buyingAsset = Asset.createNonNativeAsset(buyingAssetCode, issuer.getKeyPair());
		}

		final Account distributor = sellingToken.getDistributor();
		final String distributionAmount = String.valueOf(dto.getAmount());
		final String price = String.valueOf(dto.getPrice());
		final DistributionState distributionState = dto.getDistributionState();

		sellingToken.setPrice(dto.getPrice());

		return this.stellarService
				.setTokenOffer(distributor, sellingAsset, buyingAsset, distributionAmount, price,
						distributionState)
				.thenApply(Util.txResponseBuilder)
				.exceptionally(throwable -> {
					sellingToken.setPrice(null);
					throw new TokenDistributionException(throwable.getMessage());
				});
	}

	@Async
	public CompletableFuture<BalanceResponse> getBalance(final String assetCode, final String issuerId, final String targetId) {
		final Account target = this.accountService.find(targetId);
		final Token token = findByCodeAndIssuer(assetCode, issuerId);

		return this.stellarService.getBalance(target, token)
				.thenApply(balance -> {
					BalanceResponse balanceResponse = new BalanceResponse();
					balanceResponse.setAssetCode(assetCode);
					balanceResponse.setIssuerId(issuerId);
					balanceResponse.setTargetId(targetId);
					balanceResponse.setBalance(balance);

					return balanceResponse;
				});
	}

	Token findByCodeAndIssuer(final String code, final String issuerAccountId) {
		if (!this.tokenStorage.containsKey(code)) {
			throw new TokenNotFoundException(String.format("Token %s not found", code));
		}
		final Token token = this.tokenStorage.get(code);

		if (issuerAccountId.equals(token.getIssuer().getStellarAccountId())) {
			return token;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
