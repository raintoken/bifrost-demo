package com.raincheck.token.demoapp.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.Function;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import com.raincheck.token.demoapp.model.TransactionResponse;

public class Util {

	public static Function<SubmitTransactionResponse, TransactionResponse> txResponseBuilder = submitResponse -> {
		TransactionResponse response = new TransactionResponse();
		response.setLedger(submitResponse.getLedger());
		response.setTxHash(submitResponse.getHash());
		response.setResultXdr(submitResponse.getResultXdr());
		response.setSuccess(submitResponse.isSuccess());

		return response;
	};

	public static String formatDecimals(final String decimals) {
		DecimalFormat formatter = new DecimalFormat("#0.0000000");
		return formatter.format(new BigDecimal(decimals));
	}

}
