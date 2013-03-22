package il.co.zcredit;

import java.io.IOException;

import com.google.api.client.http.HttpRequestFactory;

public class ZcreditClient {
	private final HttpRequestFactory requestFactory;
	private final Integer connectTimeout;
	private final Integer readTimeout;
	private final String username;
	private final String password;

	public ZcreditClient(HttpRequestFactory requestFactory, Integer connectTimeout, Integer readTimeout,
			String username, String password) {
		this.requestFactory = requestFactory;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.username = username;
		this.password = password;
	}
	
	public String clearCreditCard(CreditCardPayment creditCardPayment) throws IOException, ZcreditException {
		final Authenticator authenticator = new Authenticator(
				requestFactory, connectTimeout, readTimeout, username, password);
		authenticator.authenticate();
		
		final CreditCardClearer clearer = new CreditCardClearer(
				requestFactory, connectTimeout, readTimeout, authenticator.getSessionCookie());
		return clearer.clearCreditCard(creditCardPayment);
	}
}
