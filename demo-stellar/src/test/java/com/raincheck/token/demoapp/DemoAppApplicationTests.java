package com.raincheck.token.demoapp;

import static com.raincheck.token.demoapp.util.Util.formatDecimals;
import static com.raincheck.token.demoapp.util.Util.txResponseBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raincheck.token.demoapp.dto.ChangeTrustDto;
import com.raincheck.token.demoapp.dto.CreateAccountDto;
import com.raincheck.token.demoapp.dto.CreateTokenDto;
import com.raincheck.token.demoapp.dto.IssueTokenDto;
import com.raincheck.token.demoapp.dto.LockAccountDto;
import com.raincheck.token.demoapp.dto.TokenDistributionDto;
import com.raincheck.token.demoapp.model.Account;
import com.raincheck.token.demoapp.model.Token;
import com.raincheck.token.demoapp.model.enums.DistributionState;
import com.raincheck.token.demoapp.service.AccountService;
import com.raincheck.token.demoapp.service.StellarService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DemoAppApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	StellarService stellarService;

	@Autowired
	AccountService accountService;

	@Test
	public void e2eTest() throws Exception {

		final Account issuerAccount = createAccount("issuer");
		final String issuerAccountId = issuerAccount.getStellarAccountId();

		final Account distributorAccount = createAccount("distributor");
		final String distributorAccountId = distributorAccount.getStellarAccountId();

		final Token qqqToken = createToken("QQQ", issuerAccountId);
		changeTrust(qqqToken, distributorAccountId);
		issueToken(qqqToken, distributorAccountId, issuerAccountId);

//**********************************************************************************************************
		/*
			This section is important only if you are running Bifrost for accepting ETH/BTC
		 */
		final Token ethToken = createToken("ETH", issuerAccountId);
		changeTrust(ethToken, distributorAccountId);
		issueToken(ethToken, distributorAccountId, issuerAccountId);

		final Token btcToken = createToken("BTC", issuerAccountId);
		changeTrust(btcToken, distributorAccountId);
		issueToken(btcToken, distributorAccountId, issuerAccountId);
//**********************************************************************************************************

		final MultiValueMap<String, String> distributorParams = buildParams(qqqToken.getCode(), issuerAccountId,
				distributorAccountId);

		validateBalance(distributorParams, String.valueOf(qqqToken.getAmount()));

		qqqToken.setDistributor(distributorAccount);
		qqqToken.setPrice("1");

		startTokenDistribution(qqqToken, issuerAccountId);

		final String investorName = "investor";
		final Account investor = createAccount(investorName);

		final String investmentAmount = "1000";
		sendInvestment(investor, qqqToken, investmentAmount);

		final BigDecimal investment = new BigDecimal(investmentAmount).multiply(new BigDecimal(qqqToken.getPrice()));
		final MultiValueMap<String, String> investorParams = buildParams(qqqToken.getCode(), issuerAccountId,
				investor.getStellarAccountId());
		validateBalance(investorParams, String.valueOf(investment));

		final MultiValueMap<String, String> againDistributorParams = buildParams(qqqToken.getCode(), issuerAccountId,
				distributorAccountId);
		final BigDecimal distributorExpected = BigDecimal.valueOf(qqqToken.getAmount()).subtract(investment);
		validateBalance(againDistributorParams, String.valueOf(distributorExpected));

		finishTokenDistribution(qqqToken, issuerAccountId);

		lockAccount(issuerAccountId);
	}

	/**
	 *
	 * @param tokenCode - Code of the token to be distributed. At this stage only the asset code is
	 * associated with issuer KeyPair, amount is only set for a platform Token object.
	 * @param issuerAccountId - Stellar public key of the issuer account
	 */
	private Token createToken(String tokenCode, String issuerAccountId) throws Exception {
		final CreateTokenDto tokenDto = createTokenDto(tokenCode, issuerAccountId);
		return this.mapper.readValue(this.mockMvc.perform(post("/api/token/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(tokenDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code", is(tokenDto.getAssetCode())))
				.andExpect(jsonPath("$.issuer.stellarAccountId", is(issuerAccountId)))
				.andExpect(jsonPath("$.amount", is(tokenDto.getAmount().intValue()))) //returning amount as intValue only for testing, jsonPath works bad with Long type
				.andDo(print())
				.andReturn().getResponse().getContentAsString(), Token.class);
	}

	/**
	 *
	 * @param token - A token to be trusted by a target account. Before transferring any asset,
	 * account should trust the asset and the issuer of the asset. For example, the distributor account
	 * must trust the issuer->asset pair to become the distributor of the asset.
	 * @param targetAccountId - Stellar public key of the account who is going to trust issuer->asset
	 */
	private void changeTrust(Token token, String targetAccountId) throws Exception {
		final MvcResult trustTxAsync = this.mockMvc.perform(post("/api/accounts/trust")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(changeTrustDto(token, targetAccountId))))
				.andReturn();

		validateTransactionResponse(trustTxAsync);
	}

	/**
	 *
	 * @param token - A token to be issued, e.g. specified amount of the token is sent to the distributor
	 * @param distributorId - Stellar public key of the distributor account
	 * @param issuerId - Stellar public key of the issuer account
	 */
	private void issueToken(Token token, String distributorId, String issuerId)
			throws Exception {
		final MvcResult issueTokenTxAsync = this.mockMvc.perform(post("/api/token/issue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(issueTokenDto(token, distributorId, issuerId))))
				.andReturn();

		validateTransactionResponse(issueTokenTxAsync);
	}

	/**
	 *
	 * @param token - A token to be distributed, e.g. the ManageOfferOperation is created at this step
	 * with the specified price.
	 * @param issuerId - Stellar public key of the issuer account
	 */
	private void startTokenDistribution(Token token, String issuerId) throws Exception {
		final MvcResult startTokenDistributionTxAsync = this.mockMvc.perform(post("/api/token/distribution")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(tokenDistributionDto(token, issuerId, DistributionState.START))))
				.andReturn();

		validateTransactionResponse(startTokenDistributionTxAsync);
	}

	/**
	 *
	 * @param investor - An investors account on the application side. Contains Stellar keys.
	 * @param token - A token to buy
	 * @param amount - Amount to buy
	 *
	 * As long as we do not call an endpoint for that operation we use a blocking call. Similar functionality
	 * should be implemented on a wallet side.
	 */
	private void sendInvestment(Account investor, Token token, String amount) {
		this.stellarService.investorTx(investor, token, amount).join().stream()
				.map(txResponseBuilder)
				.forEach(txResponse -> {
					assertTrue(txResponse.isSuccess());
					assertNotNull(txResponse.getLedger());
					assertNotNull(txResponse.getTxHash());
					assertNotNull(txResponse.getResultXdr());
				});
	}

	/**
	 *
	 * @param token - A token to be affected by the finishing distributing.
	 * @param issuerId -  Stellar public key of the issuer account who is the owner of the token
	 */
	private void finishTokenDistribution(Token token, String issuerId) throws Exception {
		final MvcResult finishTokenDistributionTxAsync = this.mockMvc.perform(post("/api/token/distribution")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(tokenDistributionDto(token, issuerId, DistributionState.FINISH))))
				.andReturn();

		validateTransactionResponse(finishTokenDistributionTxAsync);
	}

	/**
	 *
	 * @param accountId - Stellar public key of the account to be locked. Before calling this endpoint,
	 * account must be loaded to the application using '/api/accounts/load'. Here we do not call that
	 * endpoint because we have these accounts loaded into application's memory.
	 */
	private void lockAccount(String accountId) throws Exception {
		final MvcResult lockAccountTxAsync = this.mockMvc.perform(post("/api/accounts/lock")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.mapper.writeValueAsString(lockAccountDto(accountId))))
				.andReturn();

		validateTransactionResponse(lockAccountTxAsync);
	}

	/**
	 *
	 * @param name - Just a username of an account
	 */
	private Account createAccount(String name) throws Exception {
		return this.mapper
				.readValue(this.mockMvc.perform(post("/api/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(this.mapper.writeValueAsString(createAccountDto(name))))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.name", is(name)))
						.andDo(print())
						.andReturn().getResponse().getContentAsString(), Account.class);
	}

	private void validateBalance(final MultiValueMap<String, String> params, String expected) throws Exception {
		final MvcResult asyncResponse = this.mockMvc.perform(get("/api/token/balance")
				.params(params))
				.andReturn();

		this.mockMvc.perform(asyncDispatch(asyncResponse))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.issuerId", is(params.getFirst("issuerId"))))
				.andExpect(jsonPath("$.targetId", is(params.getFirst("targetId"))))
				.andExpect(jsonPath("$.assetCode", is(params.getFirst("assetCode"))))
				.andExpect(jsonPath("$.balance", is(formatDecimals(expected))))
				.andDo(print());
	}

	private void validateTransactionResponse(final MvcResult asyncResponse) throws Exception {
		this.mockMvc.perform(asyncDispatch(asyncResponse))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(Boolean.TRUE)))
				.andExpect(jsonPath("$.txHash", notNullValue()))
				.andExpect(jsonPath("$.resultXdr", notNullValue()))
				.andExpect(jsonPath("$.ledger", notNullValue()))
				.andDo(print());
	}

	private CreateAccountDto createAccountDto(String name) {
		CreateAccountDto dto = new CreateAccountDto();
		dto.setName(name);
		return dto;
	}

	private CreateTokenDto createTokenDto(final String code, final String issuerAccountId) {
		CreateTokenDto dto = new CreateTokenDto();
		dto.setAssetCode(code);
		dto.setAmount(500000L);
		dto.setIssuerId(issuerAccountId);
		return dto;
	}

	private ChangeTrustDto changeTrustDto(Token token, String distributorAccountId) {
		ChangeTrustDto dto = new ChangeTrustDto();
		dto.setAmount(token.getAmount());
		dto.setAssetCode(token.getCode());
		dto.setDistributorId(distributorAccountId);
		dto.setIssuerId(token.getIssuer().getStellarAccountId());

		return dto;
	}

	private IssueTokenDto issueTokenDto(Token token, String distributorId, String issuerId) {
		IssueTokenDto dto = new IssueTokenDto();
		dto.setAmount(token.getAmount());
		dto.setCode(token.getCode());
		dto.setDistributorAccountId(distributorId);
		dto.setIssuerAccountId(issuerId);

		return dto;
	}

	private TokenDistributionDto tokenDistributionDto(Token token, String issuerAccountId, DistributionState state) {
		TokenDistributionDto dto = new TokenDistributionDto();
		dto.setAmount(token.getAmount());
		dto.setPrice("1");
		dto.setIssuerAccountId(issuerAccountId);
		dto.setSellingAssetCode(token.getCode());
		dto.setBuyingAssetCode("XLM"); // Stellar native asset
		dto.setDistributionState(state);

		return dto;
	}

	private LockAccountDto lockAccountDto(String accountToLockId) {
		LockAccountDto dto = new LockAccountDto();
		dto.setAccountId(accountToLockId);

		return dto;
	}

	private MultiValueMap<String, String> buildParams(String assetCode, String issuer, String target) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("issuerId", issuer);
		params.add("targetId", target);
		params.add("assetCode", assetCode);

		return params;
	}

}
