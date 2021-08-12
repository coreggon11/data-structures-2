package exceptions;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WrongPositionDataException extends Exception {

	private WrongPositionDataException(char actual, Character... expected) {
		super(String.format("Wrong position data! Expected %s, got %s",
				Arrays.stream(expected)
						.map(String::valueOf)
						.collect(Collectors.joining(",")), actual));
	}

	public static void checkAndThrow(char actual, Character... expected) throws WrongPositionDataException {
		for (Character c : expected) {
			if (c == actual) {
				return;
			}
		}
		throw new WrongPositionDataException(actual, expected);
	}

}
