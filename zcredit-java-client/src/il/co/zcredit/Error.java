package il.co.zcredit;

public class Error {
	public static final String CODE_PROTOCOL = "protocol";
	public static final String CODE_LOGIN = "login";
	
	public static final String CODE_CARD_REFUSED = "4"; // "�����."
	public static final String CODE_CARD_EXPIRED = "36"; // "�� ����."
	public static final String CODE_CARD_INVALID = "36"; // "����� �� ����."
	public static final String CODE_CARD_TYPO = "39"; // "����� ����� �� �����."
	
	public final String code;
	public final String description;
	
	public Error(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return code + "|" + description;
	}
}
