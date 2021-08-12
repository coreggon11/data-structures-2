package application.business;

import exceptions.KeysCountDoesNotMatchException;
import exceptions.WrongPositionDataException;
import structures.KDTree;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * class which contains application data
 */
public class AppData {

	public static final String PLOTS_FILE_NAME = "plots.csv";
	public static final String ESTATES_FILE_NAME = "estates.csv";

	private final KDTree<Position.PositionPart, Estate> estateTree;
	private final KDTree<Position.PositionPart, Plot> plotTree;

	public AppData() {
		estateTree = new KDTree<>(2);
		plotTree = new KDTree<>(2);
	}

	/**
	 * add estate to system with desired position
	 *
	 * @param estate estate to be added
	 */
	public void addEstate(Estate estate) {
		try {
			estateTree.insert(estate.getPosition().getPositionAsArray(), estate);
		} catch (KeysCountDoesNotMatchException e) {
			System.out.println("Wrong position data");
		}
	}

	/**
	 * add plot to system with desired position
	 *
	 * @param plot plot to be added
	 */
	public void addPlot(Plot plot) {
		try {
			plotTree.insert(plot.getPosition().getPositionAsArray(), plot);
		} catch (KeysCountDoesNotMatchException e) {
			System.out.println("Wrong position data");
		}
	}

	/**
	 * finds all estates on the position
	 *
	 * @param position position of the estates
	 * @return list of all estates on the position
	 * @throws KeysCountDoesNotMatchException thrown by tree, but should not be thrown by the way the app works
	 */
	public List<Estate> findEstates(Position position) throws KeysCountDoesNotMatchException {
		return estateTree.get(position.getPositionAsArray());
	}

	/**
	 * finds all plots on the position
	 *
	 * @param position position of the estates
	 * @return list of all plots on the position
	 * @throws KeysCountDoesNotMatchException thrown by tree, but should not be thrown by the way the app works
	 */
	public List<Plot> findPlots(Position position) throws KeysCountDoesNotMatchException {
		return plotTree.get(position.getPositionAsArray());
	}

	/**
	 * finds all estates and plots with position lower or equal to  max and higher or equal to min
	 *
	 * @param min lower bound
	 * @param max higher bound
	 * @return list of all estates and plots with position lower or equal to  max and higher or equal to min
	 */
	public List<PositionObject> findObjects(Position min, Position max) {
		List<Estate> estates = estateTree.get(min.getPositionAsArray(), max.getPositionAsArray());
		List<Plot> plots = plotTree.get(min.getPositionAsArray(), max.getPositionAsArray());
		List<PositionObject> objects = new LinkedList<>();
		objects.addAll(estates);
		objects.addAll(plots);
		return objects;
	}

	/**
	 * finds estate on position and deletes it if it is the same instance
	 *
	 * @param estate estate to be deleted
	 * @return true if deleted, false otherwise
	 * @throws KeysCountDoesNotMatchException thrown by tree, but should not be thrown by the way the app works
	 */
	public boolean deleteEstate(Estate estate) throws KeysCountDoesNotMatchException {
		return estateTree.delete(estate.getPosition().getPositionAsArray(), estate);
	}

	/**
	 * finds plot on position and deletes it if it is the same instance
	 *
	 * @param position position of the plot
	 * @param plot     plot to be deleted
	 * @return true if deleted, false otherwise
	 * @throws KeysCountDoesNotMatchException thrown by tree, but should not be thrown by the way the app works
	 */
	public boolean deletePlot(Position position, Plot plot) throws KeysCountDoesNotMatchException {
		return plotTree.delete(position.getPositionAsArray(), plot);
	}

	/**
	 * save estates and plots to file
	 *
	 * @throws IOException when an error with file occurs
	 */
	public void save() throws IOException {
		saveTree(estateTree, ESTATES_FILE_NAME);
		saveTree(plotTree, PLOTS_FILE_NAME);
	}

	/**
	 * saves tree to file, we know the data is either plot or estate (position object)
	 * we traverse the tree with preorder traversal and save each node in format
	 * width, width position, length, length position, id of the object, description of the object
	 *
	 * @param tree     tree we want to save
	 * @param fileName file we want to save to
	 * @param <D>      type of object inside the tree
	 * @throws IOException when an error with file occurs
	 */
	private <D extends PositionObject> void saveTree(KDTree<Position.PositionPart, D> tree, String fileName) throws IOException {
		Stack<KDTree.KDTreeNode<Position.PositionPart, D>> stack = new Stack<>();
		if (tree.getRoot() != null) {
			stack.push(tree.getRoot());
		}
		File file = new File(fileName);
		if (!file.createNewFile()) {
			file.delete();
			file.createNewFile();
		}
		FileWriter writer = new FileWriter(file);
		while (!stack.isEmpty()) {
			KDTree.KDTreeNode<Position.PositionPart, D> actual = stack.pop();
			D data = actual.getData();
			Map.Entry<Character, Double> width = data.getPosition().getWidth().getEntry();
			Map.Entry<Character, Double> length = data.getPosition().getLength().getEntry();
			String line = String.format(Locale.US, "%c, %f, %c, %f, %d, %s%n",
					width.getKey(), width.getValue(), length.getKey(), length.getValue(), data.getId(), data.getDescription());
			writer.append(line);
			if (actual.getLeftSon() != null) {
				stack.push(actual.getLeftSon());
			}
			if (actual.getRightSon() != null) {
				stack.push(actual.getRightSon());
			}
		}
		writer.close();
	}

	/**
	 * loads app state from file
	 *
	 * @return true if data was loaded successfully
	 * @throws FileNotFoundException          when file does not exist
	 * @throws KeysCountDoesNotMatchException is handled on the application side
	 * @throws WrongPositionDataException     is handled on the application side
	 */
	public boolean load() throws FileNotFoundException, KeysCountDoesNotMatchException, WrongPositionDataException {
		boolean out = loadTree(estateTree, "estates.csv");
		out |= loadTree(plotTree, "plots.csv");
		return out;
	}

	/**
	 * loads tree from file
	 *
	 * @param tree     tree we want to load
	 * @param fileName file we want to load from
	 * @param <D>      type of data inside the tree
	 * @return true if data was loaded successfully
	 * @throws FileNotFoundException          when file does not exist
	 * @throws WrongPositionDataException     is handled on the application side
	 * @throws KeysCountDoesNotMatchException is handled on the application side
	 */
	private <D extends PositionObject> boolean loadTree(KDTree<Position.PositionPart, D> tree, String fileName) throws FileNotFoundException, WrongPositionDataException, KeysCountDoesNotMatchException {
		tree.clear();
		File file = new File(fileName);
		if (!file.exists()) {
			return false;
		}
		Scanner in = new Scanner(file);
		List<D> positionObjects = new ArrayList<>();
		while (in.hasNext()) {
			// %c, %f, %c, %f, %d, %s
			char w = in.next().charAt(0);
			String next = in.next();
			double wP = Double.parseDouble(next.substring(0, next.length() - 1));
			char l = in.next().charAt(0);
			next = in.next();
			double lP = Double.parseDouble(next.substring(0, next.length() - 1));
			next = in.next();
			int id = Integer.parseInt(next.substring(0, next.length() - 1));
			String desc = in.nextLine();
			Position position = new Position(w, l, wP, lP);
			if (fileName.equals("estates.csv")) {
				positionObjects.add((D) new Estate(id, desc, new ArrayList<>(), position));
			} else {
				positionObjects.add((D) new Plot(id, desc, findEstates(position), position));
			}
		}
		if (fileName.equals("estates.csv")) {
			List<Map.Entry<Position.PositionPart[], Estate>> list = positionObjects.stream()
					.map(Estate.class::cast)
					.map(object -> new AbstractMap.SimpleEntry<>(object.getPosition().getPositionAsArray(), object))
					.collect(Collectors.toList());
			estateTree.insert(list);
		} else {
			List<Map.Entry<Position.PositionPart[], Plot>> list = positionObjects.stream()
					.map(Plot.class::cast)
					.map(object -> new AbstractMap.SimpleEntry<>(object.getPosition().getPositionAsArray(), object))
					.collect(Collectors.toList());
			plotTree.insert(list);
		}
		in.close();
		return true;
	}
}
