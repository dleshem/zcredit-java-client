package il.co.zcredit;

public class ZcreditException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final Error error;
	
	public ZcreditException(Error error) {
		super(error.toString());
		this.error = error;
	}
	
	public ZcreditException(Error error, Throwable cause) {
		super(error.toString(), cause);
		this.error = error;
	}
	
	public Error getError() {
		return error;
	}
}
