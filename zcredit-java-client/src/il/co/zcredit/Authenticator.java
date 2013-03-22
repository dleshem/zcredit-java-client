package il.co.zcredit;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.UrlEncodedContent;

public class Authenticator {
	public static final String SESSION_COOKIE_NAME = "ASP.NET_SessionId";
	private static final String LOGIN_URL = "https://www.zcredit.co.il/WebControl/Login.aspx";
	
	private final HttpRequestFactory requestFactory;
	private final Integer connectTimeout;
	private final Integer readTimeout;
	private final String username;
	private final String password;
	
	private String sessionCookie;
	
	public Authenticator(HttpRequestFactory requestFactory, Integer connectTimeout, Integer readTimeout,
			String username, String password) {
		this.requestFactory = requestFactory;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.username = username;
		this.password = password;
	}
	
	private FormBuilder getForm() throws IOException, ZcreditException {
		final HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(LOGIN_URL));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout);
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		
		final HttpResponse response = request.execute();
		try {
			sessionCookie = extractSessionCookie(response);
			
			final Document doc = Jsoup.parse(response.getContent(), "UTF-8", "");
			return FormBuilder.extract(doc);
		} finally {
			response.ignore();
		}
	}
	
	public void authenticate() throws IOException, ZcreditException {
		final FormBuilder builder = getForm();
		builder.put("txt_username", username);
		builder.put("txt_password", password);
		
		final HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(LOGIN_URL),
				new UrlEncodedContent(builder.build()));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout);
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		request.setFollowRedirects(false);
		request.setThrowExceptionOnExecuteError(false);
		request.getHeaders().setCookie(SESSION_COOKIE_NAME + "=" + sessionCookie);
		
		final HttpResponse response = request.execute();
		try {
			if (!HttpStatusCodes.isRedirect(response.getStatusCode())) {
				throw new ZcreditException(new Error(Error.CODE_LOGIN, "incorrect username or password"));
			}
		} finally {
			response.ignore();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static String extractSessionCookie(HttpResponse response) {
		final List<String> setCookies = (List<String>) response.getHeaders().get("set-cookie");
		for (String setCookie : setCookies) {
			final String extractedSessionCookie = extractCookie(setCookie, SESSION_COOKIE_NAME);
			if (extractedSessionCookie != null) {
				return extractedSessionCookie;
			}
		}
		return null;
	}
	
	// buggy and ugly, but (on AppEngine) HttpCookie.parse throws java.lang.IllegalArgumentException: Illegal cookie attribute
	private static String extractCookie(String setCookie, String name) {
		final String nameToken = name + "=";
		final int index = setCookie.indexOf(nameToken);
		if (index == -1) {
			return null;
		}
		
		final int startIndex = index + nameToken.length();
		
		int endIndex = setCookie.length();
		final int i1 = setCookie.indexOf(';', startIndex);
		if (i1 != -1) {
			endIndex = Math.min(i1, endIndex);
		}
		final int i2 = setCookie.indexOf(',', startIndex);
		if (i2 != -1) {
			endIndex = Math.min(i2, endIndex);
		}
		
		return setCookie.substring(startIndex, endIndex);
	}
	
	public String getSessionCookie() {
		return sessionCookie;
	}
}
