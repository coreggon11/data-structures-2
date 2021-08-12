package exceptions;

public class KeysCountDoesNotMatchException extends Exception {

	private KeysCountDoesNotMatchException(int expected, int actual) {
		super(String.format("Keys count does not match! Structure keys count %d, got %d", expected, actual));
	}

	public static void checkAndThrow(int expected, int actual) throws KeysCountDoesNotMatchException {
		if (expected != actual) {
			throw new KeysCountDoesNotMatchException(expected, actual);
		}
	}

}
