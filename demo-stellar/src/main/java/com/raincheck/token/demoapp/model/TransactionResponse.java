package com.raincheck.token.demoapp.model;

public class TransactionResponse {

	private String txHash;
	private Long ledger;
	private String resultXdr;
	private boolean isSuccess;

	public String getTxHash() {
		return txHash;
	}

	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}

	public Long getLedger() {
		return ledger;
	}

	public void setLedger(Long ledger) {
		this.ledger = ledger;
	}

	public String getResultXdr() {
		return resultXdr;
	}

	public void setResultXdr(String resultXdr) {
		this.resultXdr = resultXdr;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean success) {
		isSuccess = success;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("TransactionResponse{");
		sb.append("txHash='").append(txHash).append('\'');
		sb.append(", ledger=").append(ledger);
		sb.append(", resultXdr='").append(resultXdr).append('\'');
		sb.append(", isSuccess=").append(isSuccess);
		sb.append('}');
		return sb.toString();
	}
}
