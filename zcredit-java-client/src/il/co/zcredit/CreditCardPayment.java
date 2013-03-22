package il.co.zcredit;

public class CreditCardPayment {
	/** Card number, e.g. "1111222233334444" */
	public String number;
	
	/** Expiration month, 1-based. */
	public Integer expireMonth;
	
	/** Expiration year (4 digits or 2 digits). */
	public Integer expireYear;
	
	/**
	 * Card Security Code (optional).
	 * @see http://en.wikipedia.org/wiki/Card_security_code
	 */
	public String csc;
	
	/** Card holder-ID (optional). */
	public String holderId;
	
	/** Amount to charge in ILS, e.g. 19.99 */
	public Double amount;
}
