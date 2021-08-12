package application.business;

import exceptions.WrongPositionDataException;
import lombok.Getter;
import lombok.Setter;

import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
/**
 * class holding info about gps position
 * each part is represented by position part which is a map entry of
 * character of position and double being the position of the position
 */
public class Position {
	private PositionPart width;
	private PositionPart length;
	public static final Character[] ALLOWED_WIDTH = {'N', 'S'};
	public static final Character[] ALLOWED_LENGTH = {'E', 'W'};

	/**
	 * @throws WrongPositionDataException if wrong position data is entered - handled on app side
	 */
	public Position(char width, char length, double widthPosition, double lengthPosition) throws WrongPositionDataException {
		WrongPositionDataException.checkAndThrow(width, ALLOWED_WIDTH);
		WrongPositionDataException.checkAndThrow(length, ALLOWED_LENGTH);
		this.width = new PositionPart(width, widthPosition);
		this.length = new PositionPart(length, lengthPosition);
	}

	/**
	 * @return String representation of the position
	 */
	public String toString() {
		return String.format(Locale.US, "W: %f%c, L: %f%c",
				width.getEntry().getValue(), width.getEntry().getKey(),
				length.getEntry().getValue(), length.getEntry().getKey());
	}

	/**
	 * @param other object to be compared
	 * @return true if objects are equal
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		Position position = (Position) other;
		return width.compareTo(position.width) == 0 && length.compareTo(position.length) == 0;
	}

	/**
	 * @return position parts inside array
	 */
	public PositionPart[] getPositionAsArray() {
		return new PositionPart[]{width, length};
	}

	/**
	 * class holding info about position part (width or length)
	 * data is inside map entry consisting of Character (N,S/E,W) and a double (position)
	 */
	public static class PositionPart implements Comparable<PositionPart> {
		@Getter
		private final Map.Entry<Character, Double> entry;

		public PositionPart(char charPart, double position) {
			this.entry = new AbstractMap.SimpleEntry<>(charPart, position);
		}

		@Override
		public int compareTo(PositionPart other) {
			int charCompare = Character.compare(entry.getKey(), other.entry.getKey());
			if (charCompare == 0) {
				return Double.compare(entry.getValue(), other.entry.getValue());
			} else {
				return charCompare < 0 ? -1 : 1;
			}
		}
	}
}
