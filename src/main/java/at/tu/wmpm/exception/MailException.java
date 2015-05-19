package at.tu.wmpm.exception;

/**
 * 
 * @author Christian
 *
 */
public class MailException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3153224263762906995L;

	public MailException() {
	}

	public MailException(String message) {
		super(message);
	}
}