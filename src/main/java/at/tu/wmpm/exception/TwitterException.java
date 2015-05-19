package at.tu.wmpm.exception;

public class TwitterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3071174993904808875L;

	public TwitterException() {
	}

	public TwitterException(String message) {
		super(message);
	}
}