package il.co.zcredit;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.UrlEncodedContent;

public class CreditCardClearer {
	private static final String TRANSACTION_URL = "https://www.zcredit.co.il/WebControl/Transaction.aspx";
	
	private final HttpRequestFactory requestFactory;
	private final Integer connectTimeout;
	private final Integer readTimeout;
	
	public CreditCardClearer(HttpRequestFactory requestFactory, Integer connectTimeout, Integer readTimeout) {
		this.requestFactory = requestFactory;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}
	
	private FormBuilder getForm(String sessionCookie) throws IOException, ZcreditException {
		final HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(TRANSACTION_URL));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout);
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		
		request.getHeaders().setCookie(Authenticator.SESSION_COOKIE_NAME + "=" + sessionCookie);
		
		final HttpResponse response = request.execute();
		try {
			final Document doc = Jsoup.parse(response.getContent(), "UTF-8", "");
			return FormBuilder.extract(doc);
		} finally {
			response.ignore();
		}
	}
	
	public String clearCreditCard(String sessionCookie, CreditCardPayment creditCardPayment) throws IOException, ZcreditException {
		final FormBuilder builder = getForm(sessionCookie);
		
		builder.put("ctl00$ContentPlaceHolder$txt_creditcardNumber", creditCardPayment.number);
		builder.put("ctl00$ContentPlaceHolder$DDL_MM", creditCardPayment.expireMonth);
		builder.put("ctl00$ContentPlaceHolder$DDL_YY",
				((creditCardPayment.expireYear != null) ? Integer.toString(creditCardPayment.expireYear % 100) : null));
		builder.put("ctl00$ContentPlaceHolder$txt_CVV", creditCardPayment.csc);
		builder.put("ctl00$ContentPlaceHolder$txt_HolderID", creditCardPayment.holderId);
		builder.put("ctl00$ContentPlaceHolder$txt_PaymentSum", creditCardPayment.amount);
		
//		builder.put("ctl00$ContentPlaceHolder$DDL_CreditType", "רגילה");
//		builder.put("ctl00$ContentPlaceHolder$DDL_CurrencyType", "ש\"ח");
//		builder.put("ctl00$ContentPlaceHolder$txt_CustomerName", "");
//		builder.put("ctl00$ContentPlaceHolder$txt_PhoneNumber2", "");
//		builder.put("ctl00$ContentPlaceHolder$txt_customerEmail", "");
//		builder.put("ctl00$ContentPlaceHolder$txt_ExtraData", "");
		
		builder.put("ctl00$ContentPlaceHolder$BtnSubmit.x", "0");
		builder.put("ctl00$ContentPlaceHolder$BtnSubmit.y", "0");
		
		final HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(TRANSACTION_URL),
				new UrlEncodedContent(builder.build()));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout);
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		request.getHeaders().setCookie(Authenticator.SESSION_COOKIE_NAME + "=" + sessionCookie);
		request.setFollowRedirects(false);
		request.setThrowExceptionOnExecuteError(false);
		
		final HttpResponse response = request.execute();
		try {
			if (!HttpStatusCodes.isRedirect(response.getStatusCode())) {
				final Document doc = Jsoup.parse(response.getContent(), "UTF-8", "");
				final Elements elements = doc.select("#ctl00_ContentPlaceHolder_omb_lblMessage").select("font");
				if (elements.size() != 1) {
					throw new ZcreditException(new Error(Error.CODE_PROTOCOL, "found " + elements.size() + " error text elements"));
				}
				throw new ZcreditException(parseError(elements.first().text()));
			}
			
			return extractId(response.getHeaders().getLocation());
		} finally {
			response.ignore();
		}
	}
	
	private static String extractId(String location) {
		if (location == null) {
			return "";
		}
		
		// Expected format: /WebControl/SuccessPage.aspx?ID=12345&Sum=1.0&Cnum=1234&Cname=%d7%9c%d7%90%d7%95%d7%9e%d7%99 %d7%a7%d7%90%d7%a8%d7%93 %d7%95%d7%99%d7%96%d7%94&Cur=1&IsRefund=False&Payments=1
		final int index1 = location.indexOf("ID=");
		if (index1 == -1) {
			return "";
		}
		final int start = index1 + 3;
		
		final int index2 = location.indexOf('&', start);
		return ((index2 != -1) ? location.substring(start, index2) : location.substring(start));
	}
	
	private static Error parseError(String errorText) throws ZcreditException {
		if (errorText == null) {
			throw new ZcreditException(new Error(Error.CODE_PROTOCOL, "invalid error text"));
		}
		
		final int index = errorText.indexOf(" - ");
		if (index == -1) {
			throw new ZcreditException(new Error(Error.CODE_PROTOCOL, "invalid error text format: \"" + errorText + "\""));
		}
		
		return new Error(errorText.substring(0, index), errorText.substring(index + 3));
	}
}
