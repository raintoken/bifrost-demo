package com.raincheck.token.demoapp.controller;

import com.raincheck.token.demoapp.dto.ChangeTrustDto;
import com.raincheck.token.demoapp.dto.CreateAccountDto;
import com.raincheck.token.demoapp.dto.LoadAccountDto;
import com.raincheck.token.demoapp.dto.LockAccountDto;
import com.raincheck.token.demoapp.exceptions.AccountNotFoundException;
import com.raincheck.token.demoapp.exceptions.ChangeTrustException;
import com.raincheck.token.demoapp.exceptions.LockAccountException;
import com.raincheck.token.demoapp.model.Account;
import com.raincheck.token.demoapp.model.TransactionResponse;
import com.raincheck.token.demoapp.service.AccountService;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(final AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<Account> create(@Valid @RequestBody CreateAccountDto createAccountDto) {
		final Account account = this.accountService.create(createAccountDto);
		return ResponseEntity.ok(account);
	}

	@PostMapping("/load")
	public ResponseEntity<Account> load(@Valid @RequestBody LoadAccountDto dto) {
		final Account account = this.accountService.loadExistingAccount(dto);
		return ResponseEntity.ok(account);
	}

	@PostMapping("/trust")
	public CompletableFuture<ResponseEntity<TransactionResponse>> changeTrust(
			@Valid @RequestBody ChangeTrustDto dto) {
		try {
			return this.accountService.changeTrust(dto).thenApply(ResponseEntity::ok);
		} catch (ChangeTrustException e) {
			return CompletableFuture
					.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
		} catch (AccountNotFoundException e) {
			return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
		}
	}

	@PostMapping("/lock")
	public CompletableFuture<ResponseEntity<?>> lockAccount(@Valid @RequestBody LockAccountDto dto) {
		try {
			return this.accountService.lockAccount(dto).thenApply(ResponseEntity::ok);
		} catch (LockAccountException e) {
			return CompletableFuture.completedFuture(
					ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
		} catch (AccountNotFoundException e) {
			return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
		}
	}

}
