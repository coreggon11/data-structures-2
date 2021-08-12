package application.business;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import structures.ExtensibleHashing;

import java.io.*;
import java.util.BitSet;
import java.util.Locale;

@AllArgsConstructor
@Getter
@Setter
public class Property implements ExtensibleHashing.Hashable<Property> {
	public static final int DESC_LENGTH = 20;
	public static final Property INSTANCE = new Property();

	private int id;
	private int listingNumber;
	private String description;
	private double[] gps1;
	private double[] gps2;

	public Property(int id) {
		this();
		this.id = id;
	}

	private Property() {
		id = 0;
		listingNumber = 0;
		description = " ".repeat(DESC_LENGTH);
		gps1 = new double[2];
		gps2 = new double[2];
	}

	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		Property property = (Property) other;
		if (gps1.length != property.gps1.length || gps2.length != property.gps2.length) {
			return false;
		}
		for (int i = 0; i < gps1.length; ++i) {
			if (gps1[i] != property.gps1[i]) {
				return false;
			}
		}
		for (int i = 0; i < gps2.length; ++i) {
			if (gps2[i] != property.gps2[i]) {
				return false;
			}
		}
		String thisDesc = description;
		String otherDesc = property.description;
		if (description.length() < DESC_LENGTH) {
			thisDesc = description.concat(" ".repeat(DESC_LENGTH - description.length()));
		}
		if (property.description.length() < DESC_LENGTH) {
			otherDesc = property.description.concat(" ".repeat(DESC_LENGTH - property.description.length()));
		}
		return id == property.id && listingNumber == property.listingNumber && thisDesc.equals(otherDesc);
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "Property %d (no. %d), %s", id, listingNumber, description);
	}

	@Override
	public BitSet getHash() {
		return BitSet.valueOf(new long[]{id});
	}

	@Override
	public boolean isEqual(Property data) {
		return id == data.id;
	}

	@Override
	public Property getInstance() {
		return new Property();
	}

	@Override
	public boolean isEmpty() {
		return id == 0;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(id);
		dos.writeInt(listingNumber);
		if (description.length() < DESC_LENGTH) {
			description += " ".repeat(DESC_LENGTH - description.length());
		}
		dos.writeChars(description);
		for (double pos : gps1) {
			dos.writeDouble(pos);
		}
		for (double pos : gps2) {
			dos.writeDouble(pos);
		}
		return baos.toByteArray();
	}

	@Override
	public void fromByteArray(byte[] bytes) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		id = dis.readInt();
		listingNumber = dis.readInt();
		description = "";
		for (int i = 0; i < DESC_LENGTH; ++i) {
			description = description.concat(String.valueOf(dis.readChar()));
		}
		for (int i = 0; i < 2; ++i) {
			gps1[i] = dis.readDouble();
		}
		for (int i = 0; i < 2; ++i) {
			gps2[i] = dis.readDouble();
		}
	}

	@Override
	public int getSize() {
		return 2 * Integer.BYTES + DESC_LENGTH * Character.BYTES + 4 * Double.BYTES;
	}
}

