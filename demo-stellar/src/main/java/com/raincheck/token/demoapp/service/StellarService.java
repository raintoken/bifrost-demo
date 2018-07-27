package com.raincheck.token.demoapp.service;

import static com.raincheck.token.demoapp.util.Util.formatDecimals;

import com.raincheck.token.demoapp.exceptions.BalanceException;
import com.raincheck.token.demoapp.exceptions.OfferNotFoundException;
import com.raincheck.token.demoapp.model.Account;
import com.raincheck.token.demoapp.model.Token;
import com.raincheck.token.demoapp.model.enums.DistributionState;
import com.raincheck.token.demoapp.model.enums.NetworkType;
import com.raincheck.token.demoapp.model.enums.TransactionType;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.ManageOfferOperation;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.SetOptionsOperation;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.AccountResponse.Balance;
import org.stellar.sdk.responses.OfferResponse;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.SubmitTransactionResponse;

@Service
public class StellarService {

	private final String friendBotUrl;

	private final Server server;

	public StellarService(@Value("${stellar.networkType}") final NetworkType networkType,
			@Value("${stellar.horizon.url}") final String horizonUrl,
			@Value("${stellar.friendbot.url}") final String friendBotUrl) {
		switch (networkType) {
			case MAIN:
				Network.usePublicNetwork();
				break;
			case TEST:
				Network.useTestNetwork();
				break;
			default:
				throw new IllegalArgumentException(String.format("Undefined %s networkType.", networkType));
		}
		this.friendBotUrl = friendBotUrl;
		this.server = new Server(horizonUrl);
	}

	KeyPair createAccount() {
		try {
			final KeyPair keyPair = KeyPair.random();
			final String friendBotUrlFormatted = String.format(this.friendBotUrl, keyPair.getAccountId());
			new URL(friendBotUrlFormatted).openStream();
			return keyPair;
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	KeyPair loadAccount(final String seed) {
		return KeyPair.fromSecretSeed(seed);
	}

	CompletableFuture<SubmitTransactionResponse> setTokenOffer(final Account distributor,
			final Asset selling,
			final Asset buying,
			final String amount,
			final String price,
			final DistributionState distributionState) {

		final String tokenAmount;
		final Long offerId;
		final TransactionType transactionType;

		/*
			Stellar platform has no explicit operation to cancel offer (stop token distribution).
			To do so, there must be created a new ManageOfferOperation for the same asset from the same issuer,
			but with some logic put to the Offer ID and amount.
			When a new offer (start distribution) is created, 0 must be an Offer ID. 0 (zero) indicates that
			a new offer should be created on the Stellar platform.
			But, when you want to cancel an existing offer, you have to query account's offers and the needed one
			and get it's Offer ID.
			Then, create a new ManageOfferOperation, but set 0 (zero) amount for this offer and set Offer ID
			found on the previous step.
		 */
		switch (distributionState) {
			case START:
				tokenAmount = amount;
				offerId = 0L;
				transactionType = TransactionType.TOKEN_DISTRIBUTION_START;
				break;
			case FINISH:
				tokenAmount = String.valueOf(0);
				offerId = getOfferId(distributor, selling, buying, price);
				transactionType = TransactionType.TOKEN_DISTRIBUTION_FINISH;
				break;
			default:
				throw new IllegalArgumentException();
		}

		final KeyPair keyPair = distributor.getKeyPair();
		final AccountResponse distributorAccount = getAccountResponse(keyPair);
		final Transaction tokenDistributionTx = new Transaction.Builder(distributorAccount)
				.addOperation(new ManageOfferOperation.Builder(selling, buying, tokenAmount, price)
						.setOfferId(offerId)
						.build())
				.build();

		tokenDistributionTx.sign(keyPair);
		return submitTransaction(tokenDistributionTx, transactionType);
	}

	private Long getOfferId(final Account distributor,
			final Asset selling,
			final Asset buying,
			final String price) {
		try {
			final Page<OfferResponse> execute = this.server.offers().forAccount(distributor.getKeyPair())
					.limit(50).execute();

			return execute.getRecords().stream()
					.filter(offerResponse -> offerResponse.getSelling().equals(selling)
							&& offerResponse.getBuying().equals(buying)
							&& offerResponse.getPrice().equals(formatDecimals(price))
							&& offerResponse.getSeller().getAccountId().equals(distributor.getStellarAccountId()))
					.map(OfferResponse::getId)
					.findFirst()
					.orElseThrow(() -> new OfferNotFoundException("Offer not found"));
		} catch (IOException e) {
			throw new OfferNotFoundException("Failed to retrieve offers");
		}
	}

	/*
		Lock account operation simply means to set a master weight to 0 (zero).
		Be careful, that operation cannot be reversed.
	 */
	CompletableFuture<SubmitTransactionResponse> lockAccount(final Account issuer) {
		final KeyPair keyPair = issuer.getKeyPair();
		final AccountResponse sourceAccount = getAccountResponse(keyPair);
		final Transaction lockAccountTx = new Transaction.Builder(sourceAccount)
				.addOperation(new SetOptionsOperation.Builder()
						.setMasterKeyWeight(0)
						.build())
				.build();
		lockAccountTx.sign(keyPair);

		return submitTransaction(lockAccountTx, TransactionType.LOCK_ACCOUNT);
	}

	CompletableFuture<SubmitTransactionResponse> changeTrust(final Account distributor,
			final Token token, final String amount) {
		final KeyPair keyPair = distributor.getKeyPair();
		final AccountResponse sourceAccount = getAccountResponse(keyPair);

		final Transaction changeTrustTx = new Transaction.Builder(sourceAccount)
				.addOperation(new ChangeTrustOperation.Builder(token.getAsset(), amount).build())
				.build();
		changeTrustTx.sign(keyPair);
		return submitTransaction(changeTrustTx, TransactionType.CHANGE_TRUST);
	}

	CompletableFuture<SubmitTransactionResponse> issueToken(final Token asset) {
		final Account issuer = asset.getIssuer();
		final Account distributor = asset.getDistributor();
		final String amount = String.valueOf(asset.getAmount());
		final KeyPair issuerKeyPair = issuer.getKeyPair();
		final AccountResponse issuerAccountResponse = getAccountResponse(issuerKeyPair);
		final Transaction createTokenTx = new Transaction.Builder(issuerAccountResponse)
				.addOperation(
						new PaymentOperation.Builder(distributor.getKeyPair(), asset.getAsset(), amount)
								.build())
				.build();
		createTokenTx.sign(issuerKeyPair);

		return submitTransaction(createTokenTx, TransactionType.CREATE_TOKEN);
	}

	CompletableFuture<String> getBalance(final Account target, final Token token) {
		return CompletableFuture.supplyAsync(() -> {
			final Balance[] balances = getAccountResponse(target.getKeyPair()).getBalances();
			return Arrays.stream(balances)
				.filter(balance -> balance.getAssetCode().equals(token.getCode())
						&& balance.getAssetIssuer().getAccountId().equals(token.getIssuer().getStellarAccountId()))
				.map(balance -> formatDecimals(balance.getBalance()))
				.findFirst()
				.orElseThrow(() -> new
						BalanceException(String.format("Failed to load %s balance of %s account", token.getCode(), target.getStellarAccountId())));
		});
	}

	public CompletableFuture<List<SubmitTransactionResponse>> investorTx(final Account account,
			final Token token, final String amount) {
		return CompletableFuture.supplyAsync(() -> {
			final Asset asset = token.getAsset();
			final SubmitTransactionResponse trustTx = changeTrust(account, token, amount).join();
			final SubmitTransactionResponse offerTx = sendInvestment(account, asset, amount,
					token.getPrice())
					.join();

			return Arrays.asList(trustTx, offerTx);
		});
	}

	private CompletableFuture<SubmitTransactionResponse> sendInvestment(final Account account,
			final Asset buying, final String amount, final String price) {
		final AccountResponse investor = getAccountResponse(account.getKeyPair());
		final Transaction investorOfferTx = new Transaction.Builder(investor)
				.addOperation(new ManageOfferOperation.Builder(new AssetTypeNative(), buying, amount, price)
						.setOfferId(0)
						.build())
				.build();
		investorOfferTx.sign(account.getKeyPair());
		return submitTransaction(investorOfferTx, TransactionType.INVESTMENT);
	}

	private CompletableFuture<SubmitTransactionResponse> submitTransaction(
			final Transaction transaction, final TransactionType transactionType) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return this.server.submitTransaction(transaction);
			} catch (IOException e) {
				throw new RuntimeException(
						String.format("Transaction %s failed to execute", transactionType), e);
			}
		});
	}

	private AccountResponse getAccountResponse(final KeyPair keyPair) {
		try {
			return this.server.accounts().account(keyPair);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
