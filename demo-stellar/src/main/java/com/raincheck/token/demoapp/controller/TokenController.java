package com.raincheck.token.demoapp.controller;

import com.raincheck.token.demoapp.dto.CreateTokenDto;
import com.raincheck.token.demoapp.dto.IssueTokenDto;
import com.raincheck.token.demoapp.dto.TokenDistributionDto;
import com.raincheck.token.demoapp.exceptions.BalanceException;
import com.raincheck.token.demoapp.exceptions.TokenDistributionException;
import com.raincheck.token.demoapp.exceptions.TokenIssueException;
import com.raincheck.token.demoapp.exceptions.TokenNotFoundException;
import com.raincheck.token.demoapp.model.BalanceResponse;
import com.raincheck.token.demoapp.model.Token;
import com.raincheck.token.demoapp.model.TransactionResponse;
import com.raincheck.token.demoapp.service.TokenService;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/token")
public class TokenController {

	private final TokenService tokenService;

	public TokenController(final TokenService tokenService) {
		this.tokenService = tokenService;
	}

	@PostMapping("/create")
	public ResponseEntity<Token> issueToken(@Valid @RequestBody CreateTokenDto dto) {
		final Token token = this.tokenService.createToken(dto);
		return ResponseEntity.ok(token);
	}

	@PostMapping("/issue")
	public CompletableFuture<ResponseEntity<TransactionResponse>> issueToken(
			@Valid @RequestBody IssueTokenDto issueTokenDto) {
		try {
			return this.tokenService.issueToken(issueTokenDto).thenApply(ResponseEntity::ok);
		} catch (TokenIssueException e) {
			return CompletableFuture
					.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
		}
	}

	@PostMapping("/distribution")
	public CompletableFuture<ResponseEntity<TransactionResponse>> distributeToken(
			@Valid @RequestBody TokenDistributionDto dto) {
		try {
			return this.tokenService.manageTokenDistribution(dto).thenApply(ResponseEntity::ok);
		} catch (TokenDistributionException e) {
			return CompletableFuture
					.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
		}
	}

	@GetMapping("/balance")
	public CompletableFuture<ResponseEntity<?>> getBalance(@RequestParam String assetCode,
																												 @RequestParam String issuerId,
																												 @RequestParam String targetId) {
		try {
			return this.tokenService.getBalance(assetCode, issuerId, targetId).thenApply(ResponseEntity::ok);
		} catch (TokenNotFoundException e) {
			return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
		} catch (IllegalArgumentException e) {
			return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(e.getMessage()));
		} catch (BalanceException e) {
			return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
		}
	}

}
