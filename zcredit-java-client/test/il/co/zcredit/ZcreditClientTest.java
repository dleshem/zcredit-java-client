package il.co.zcredit;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;

import org.junit.Test;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

public class ZcreditClientTest {
	private static HttpRequestFactory buildRequestFactory() {
		try {
			return new NetHttpTransport.Builder().doNotValidateCertificate().build().createRequestFactory();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private static final HttpRequestFactory requestFactory = buildRequestFactory();
	private static final ZcreditClient zcredit = new ZcreditClient(requestFactory, 10000, 10000);
	
	// TOOD: use real values
	private static final Credentials credentials = new Credentials("XXX", "XXX");

	@Test
	public void testSaleWrongCredentials() throws Exception {
		try {
			zcredit.sale(new Credentials("test", "123"), null);
			fail("Expected exception.");
		} catch (ZcreditException e) {
			assertEquals(Error.CODE_LOGIN, e.getError().code);
		}
	}

	@Test
	public void testSaleWrongCard() throws Exception {
		final CreditCardPayment payment = new CreditCardPayment();
		payment.amount = 1.0;
		payment.number = "4580458045804580";
		payment.csc = "123";
		payment.expireYear = 2020;
		payment.expireMonth = 1;
		payment.holderId = "0";
		
		try {
			zcredit.sale(credentials, payment);
		} catch (ZcreditException e) {
			assertEquals("3", e.getError().code); // "3" = "call credit card company"
		}
	}
}
