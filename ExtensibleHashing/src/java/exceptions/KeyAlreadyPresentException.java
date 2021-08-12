package exceptions;

public class KeyAlreadyPresentException extends Exception {

	public static final KeyAlreadyPresentException EXCEPTION = new KeyAlreadyPresentException();

	private KeyAlreadyPresentException() {
		super("Key already present in table!");
	}
}
