package main;

import application.business.Property;
import exceptions.KeyAlreadyPresentException;
import structures.ExtensibleHashing;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AppData {

	private ExtensibleHashing<Property> properties;

	public AppData() {
		try {
			properties = new ExtensibleHashing<>("properties.bin", 16, 16, Property.INSTANCE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insertProperty(Property property) throws IOException, KeyAlreadyPresentException {
		properties.insert(property);
	}

	public Property findProperty(Property property) throws IOException {
		return properties.find(property);
	}

	public void update(Property oldProperty, Property newProperty) throws IOException, KeyAlreadyPresentException {
		properties.update(oldProperty, newProperty);
	}

	public Property delete(Property property) throws IOException {
		return properties.delete(property);
	}

	public List<ExtensibleHashing.Block<Property>> showDb() throws IOException {
		return properties.getAllBlocks();
	}

	public void exit() throws IOException {
		properties.close();
	}

	public void generate() throws IOException {
		properties.clear();
		Random r = new Random();
		for (int i = 0; i < 50000; ++i) {
			int id = r.nextInt() / 1000;
			if (id < 0) {
				id *= -1;
			}
			if (id == 0) {
				id++;
			}
			try {
				properties.insert(randomProperty(id, r));
			} catch (KeyAlreadyPresentException ignored) {
			}
		}
	}

	private static Property randomProperty(int id, Random r) {
		String string = "";
		int length = r.nextInt(11) + 10;
		for (int i = 0; i < length; ++i) {
			string += (char) (r.nextInt(26) + 97);
		}
		return new Property(id, r.nextInt(20000), string, new double[]{r.nextDouble() * 90, r.nextDouble() * 90 + 90},
				new double[]{r.nextDouble() * 90, r.nextDouble() * 90 + 90});
	}
}
