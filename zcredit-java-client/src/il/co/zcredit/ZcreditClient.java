package il.co.zcredit;

import java.io.IOException;

import com.google.api.client.http.HttpRequestFactory;

/**
 * A non-official zcredit.co.il web-client.
 * 
 * Works in two steps:
 * 1) Creates a new authenticated browsing session with www.zcredit.co.il
 * 2) Clears a credit payment using the in-site form.
 */
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
	
	/**
	 * @param creditCardPayment   Credit-card payment to clear.
	 * @return the payment's unique ID
	 */
	public String clearCreditCard(CreditCardPayment creditCardPayment) throws IOException, ZcreditException {
		final Authenticator authenticator = new Authenticator(
				requestFactory, connectTimeout, readTimeout, username, password);
		authenticator.authenticate();
		
		final CreditCardClearer clearer = new CreditCardClearer(
				requestFactory, connectTimeout, readTimeout, authenticator.getSessionCookie());
		return clearer.clearCreditCard(creditCardPayment);
	}
}
