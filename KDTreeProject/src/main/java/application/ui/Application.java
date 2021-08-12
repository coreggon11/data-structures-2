package application.ui;

import application.business.*;
import exceptions.KeysCountDoesNotMatchException;
import exceptions.WrongPositionDataException;
import structures.KDTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Application extends JFrame {

	public static final String APP_NAME = "Geodapp";
	public static final String VERSION = "1.0";

	public static final int WIDTH = 1470;
	public static final int HEIGHT = 600;

	private final AppUI appUi;
	private final AppData appData;

	public Application(boolean testing) {
		super(String.format("%s v %s", APP_NAME, VERSION));

		Dimension size = new Dimension(WIDTH, HEIGHT);

		this.setSize(size);
		this.setMinimumSize(size);
		this.setPreferredSize(size);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.appUi = new AppUI(this, size);
		this.appData = new AppData();

		if (testing) {
			try {
				randomOperationTest(50000000, 20000);
			} catch (KeysCountDoesNotMatchException ignored) {
			}
		}


		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				appUi.stop();
			}
		});

		this.add(appUi);

		this.pack();

		this.setVisible(true);
		appUi.run();
	}

	private Position getRandomPosition(Random r) {
		char width = r.nextBoolean() ? 'N' : 'S';
		char length = r.nextBoolean() ? 'E' : 'W';
		int widthPosition = (int) (r.nextDouble() * 90);
		int lengthPosition = (int) (r.nextDouble() * 90) + 90;
		try {
			return new Position(width, length, widthPosition, lengthPosition);
		} catch (WrongPositionDataException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void populate(int count) throws KeysCountDoesNotMatchException {
		Random r = new Random();
		int estateId = 1;
		int plotId = 1;
		for (int i = 0; i < count; ++i) {
			boolean estate = r.nextBoolean();
			Position position = getRandomPosition(r);
			if (estate) {
				addEstate(position, estateId, String.format("Random estate %d", estateId++));
			} else {
				addPlot(position, plotId, String.format("Random plot %d", plotId++));
			}
		}
	}

	public void randomOperationTest(int operationCount, int maxKey) throws KeysCountDoesNotMatchException {
		int seed = new Random().nextInt();
		Random r = new Random(seed);
		System.out.println("Seed: " + seed);
		KDTree<Integer, Integer> testTree = new KDTree<>(2);
		List<Integer[]> insertedKeys = new ArrayList<>(operationCount);
		List<Integer> insertedData = new ArrayList<>(operationCount);
		int size = 0;
		int data = 0;
		int wrong = 0;
		int treeSize = 0;
		for (int i = 0; i < operationCount; ++i) {
			switch (r.nextInt(4)) {
				case 0:
					// insert
					++data;
					Integer[] keys = new Integer[]{r.nextInt(maxKey), r.nextInt(maxKey)};
					insertedKeys.add(keys);
					insertedData.add(data);
					testTree.insert(keys, data);
					size++;
					// counting size and tree size should be equal
					List<Integer> foundData = testTree.get(keys);
					if (testTree.getSize() != size) {
						System.out.printf("Sizes do not match!%n");
						++wrong;
					} else if (foundData.size() == 0) {
						// we should be able to find the data
						System.out.printf("Inserted data not found!%n");
						++wrong;
					} else {
						// our inserted data should be inside found data
						boolean found = false;
						for (Integer integer : foundData) {
							if (integer.equals(data)) {
								found = true;
								break;
							}
						}
						if (!found) {
							System.out.printf("Inserted data not found!%n");
							++wrong;
						}
					}
					break;
				case 1:
					// get
					int index = r.nextInt(size == 0 ? 1 : insertedKeys.size());
					foundData = testTree.get(size == 0 ? new Integer[]{r.nextInt(maxKey), r.nextInt(maxKey)} : insertedKeys.get(index));
					if (size == 0 && foundData.size() != 0) {
						// we shouldnt be able to find anything
						System.out.printf("Found something!%n");
						++wrong;
					} else if (foundData.size() == 0 && size != 0) {
						// we should be able to find the data
						System.out.printf("Data not found!%n");
						++wrong;
					} else if (size != 0) {
						// our inserted data should be inside found data
						boolean found = false;
						for (Integer integer : foundData) {
							if (integer.equals(insertedData.get(index))) {
								found = true;
								break;
							}
						}
						if (!found) {
							System.out.printf("Inserted data not found!%n");
							++wrong;
						}
					}
					break;
				case 2:
					// delete
					index = r.nextInt(size == 0 ? 1 : insertedKeys.size());
					keys = new Integer[]{r.nextInt(maxKey), r.nextInt(maxKey)};
					boolean deleted = testTree.delete(size == 0 ? keys : insertedKeys.get(index),
							size == 0 ? r.nextInt(maxKey) : insertedData.get(index));
					// counting size and tree size should be equal
					foundData = testTree.get(keys);
					if (deleted) {
						--size;
						insertedKeys.remove(index);
						insertedData.remove(insertedData.get(index));
					}
					if (testTree.getSize() != size) {
						System.out.printf("Sizes do not match!%n");
						++wrong;
					} else if (deleted) {
						// we shouldnt be able to find data
						boolean found = false;
						for (Integer integer : foundData) {
							if (integer.equals(data)) {
								System.out.printf("Inserted data not deleted!%n");
								++wrong;
								break;
							}
						}
					}
					break;
				case 3:
					// get all
					foundData = testTree.get(new Integer[]{0, 0}, new Integer[]{maxKey, maxKey});
					if (foundData.size() != size) {
						// we should be able to find all  data
						System.out.printf("Different amount of data found!%n");
						++wrong;
					}
				default:
					break;
			}
		}
		System.out.printf("Test finished. %f percent of operations went wrong", (double) wrong / operationCount);
	}

	public void edit(PositionObject object, int newId, String newDesc, Position newPosition) throws KeysCountDoesNotMatchException {
		object.setId(newId);
		object.setDescription(newDesc);
		if (!object.getPosition().equals(newPosition)) {
			delete(object);
			object.setPosition(newPosition);
			if (object instanceof Plot) {
				addPlot(newPosition, newId, newDesc);
			} else {
				addEstate(newPosition, newId, newDesc);
			}
		}
	}

	public void save() throws IOException {
		appData.save();
	}

	public boolean load() throws FileNotFoundException, WrongPositionDataException, KeysCountDoesNotMatchException {
		return appData.load();
	}

	public boolean delete(PositionObject object) throws KeysCountDoesNotMatchException {
		object.clearList();
		if (object instanceof Estate) {
			return deleteEstate((Estate) object);
		} else {
			return deletePlot(object.getPosition(), (Plot) object);
		}
	}

	public boolean deleteEstate(Estate estate) throws KeysCountDoesNotMatchException {
		return appData.deleteEstate(estate);
	}

	public boolean deletePlot(Position position, Plot plot) throws KeysCountDoesNotMatchException {
		return appData.deletePlot(position, plot);
	}

	public void addEstate(Position position, int id, String description) throws KeysCountDoesNotMatchException {
		appData.addEstate(new Estate(id, description, findPlots(position), position));
	}

	public void addPlot(Position position, int id, String description) throws KeysCountDoesNotMatchException {
		appData.addPlot(new Plot(id, description, findEstates(position), position));
	}

	public List<Estate> findEstates(Position position) throws KeysCountDoesNotMatchException {
		return appData.findEstates(position);
	}

	public List<Plot> findPlots(Position position) throws KeysCountDoesNotMatchException {
		return appData.findPlots(position);
	}

	public List<PositionObject> findObjects(Position min, Position max) throws KeysCountDoesNotMatchException {
		return appData.findObjects(min, max);
	}
}
