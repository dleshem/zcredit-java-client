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
	private Authenticator authenticator;
	private CreditCardClearer clearer;

	public ZcreditClient(HttpRequestFactory requestFactory, Integer connectTimeout, Integer readTimeout) {
		authenticator = new Authenticator(requestFactory, connectTimeout, readTimeout);
		clearer = new CreditCardClearer(requestFactory, connectTimeout, readTimeout);
	}
	
	/**
	 * @param creditCardPayment   Credit-card payment to clear.
	 * @return the payment's unique ID
	 */
	public String sale(Credentials credentials, CreditCardPayment creditCardPayment) throws IOException, ZcreditException {
		final String sessionCookie = authenticator.authenticate(credentials);
		return clearer.clearCreditCard(sessionCookie, creditCardPayment);
	}
}
