package structures;

import exceptions.KeysCountDoesNotMatchException;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.List;

/**
 * @param <K> type of keys being stored
 * @param <D> type of values being stored
 */
public class KDTree<K extends Comparable<K>, D> {

	@Getter
	private KDTreeNode<K, D> root;
	private final int dimension;

	@Getter
	private int size;

	public KDTree(int dimension) {
		root = null;
		this.dimension = dimension;
		size = 0;
	}

	@Getter
	@Setter
	public static class KDTreeNode<K extends Comparable<K>, D> {

		private K[] keys;
		private D data;

		private final int dimension;

		private KDTreeNode<K, D> parent;
		private KDTreeNode<K, D> leftSon;
		private KDTreeNode<K, D> rightSon;

		public KDTreeNode(int dimension, K[] keys, D data) {
			this.dimension = dimension;
			this.keys = keys;
			this.data = data;
			parent = null;
			leftSon = null;
			rightSon = null;
		}

		public boolean isLeaf() {
			return leftSon == null && rightSon == null;
		}

		public boolean isLeftSon() {
			return parent.getLeftSon() == this;
		}

		public K getKey(int level) {
			return keys[level % dimension];
		}

		public boolean keysEqual(K[] otherKeys) throws KeysCountDoesNotMatchException {
			KeysCountDoesNotMatchException.checkAndThrow(dimension, otherKeys.length);
			for (int i = 0; i < keys.length; ++i) {
				if (keys[i].compareTo(otherKeys[i]) != 0) {
					return false;
				}
			}
			return true;
		}

		public boolean keysInRange(K[] min, K[] max) {
			for (int i = 0; i < min.length; ++i) {
				if (keys[i].compareTo(min[i]) < 0 || keys[i].compareTo(max[i]) > 0) {
					return false;
				}
			}
			return true;
		}

		public static <K extends Comparable<K>, D> int compareAtLevel(KDTreeNode<K, D> first, KDTreeNode<K, D> second, int level) {
			return first.getKey(level).compareTo(second.getKey(level));
		}

	}

	public void insert(K[] keys, D data) throws KeysCountDoesNotMatchException {
		insert(new KDTreeNode<>(dimension, keys, data));
	}

	protected void insert(KDTreeNode<K, D> node) throws KeysCountDoesNotMatchException {
		KeysCountDoesNotMatchException.checkAndThrow(dimension, node.getKeys().length);
		if (root == null) {
			// insert root
			root = node;
		} else {
			// insert node
			KDTreeNode<K, D> actual = root;
			boolean found = false;
			for (int level = 0; !found; ++level) {
				if (node.getKey(level).compareTo(actual.getKey(level)) <= 0) {
					// left son
					if (actual.getLeftSon() == null) {
						actual.setLeftSon(node);
						node.setParent(actual);
						found = true;
					} else {
						actual = actual.getLeftSon();
					}
				} else {
					// right son
					if (actual.getRightSon() == null) {
						actual.setRightSon(node);
						node.setParent(actual);
						found = true;
					} else {
						actual = actual.getRightSon();
					}
				}
			}
		}
		++size;
	}

	@SafeVarargs
	public final void insert(Map.Entry<K[], D>... entries) throws KeysCountDoesNotMatchException {
		insert(Arrays.asList(entries));
	}

	public final void insert(List<Map.Entry<K[], D>> entries) throws KeysCountDoesNotMatchException {
		// first map the list to nodes which are not accessible to user
		List<KDTreeNode<K, D>> nodes = entries.stream()
				.map(entry -> new KDTreeNode<>(dimension, entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		int itemsOnLevel = 1;
		int level = 0;
		while (nodes.size() > 0) {
			if (nodes.size() <= itemsOnLevel) {
				for (KDTreeNode<K, D> node : nodes) {
					insert(node);
				}
				break;
			} else {
				// sort by level-th key
				int levelNow = level;
				nodes.sort((first, second) -> KDTreeNode.compareAtLevel(first, second, levelNow));
				// divide nodes to items on level parts and get median from the part
				List<KDTreeNode<K, D>> newList = new ArrayList<>(nodes.size());
				int lastIndex = 0;
				for (int i = 0; i < itemsOnLevel; ++i) {
					double partSize = (double) nodes.size() / itemsOnLevel;
					int index = (int) Math.floor((i * partSize) + partSize / 2);
					newList.addAll(nodes.subList(lastIndex, index));
					lastIndex = index + 1;
					KDTreeNode<K, D> node = nodes.get(index);
					insert(node);
				}
				newList.addAll(nodes.subList(lastIndex, nodes.size()));
				nodes = newList;
			}
			itemsOnLevel *= 2;
			++level;
		}

	}

	@SafeVarargs
	public final List<D> get(K... keys) throws KeysCountDoesNotMatchException {
		ArrayList<D> data = new ArrayList<>();
		KDTreeNode<K, D> actual = root;
		int level = 0;
		while (actual != null) {
			if (actual.keysEqual(keys)) {
				data.add(actual.getData());
			}
			if (keys[level % dimension].compareTo(actual.getKey(level)) > 0) {
				actual = actual.getRightSon();
			} else {
				actual = actual.getLeftSon();
			}
			++level;
		}
		return data;
	}

	public List<D> get(K[] min, K[] max) {
		KDTreeNode<K, D> actual = root;
		KDTreeNode<K, D> fromRight = null;
		KDTreeNode<K, D> fromLeft = null;
		int level = 0;
		List<D> output = new LinkedList<>();
		while (actual != null) {
			boolean goLeft = actual.getLeftSon() != null && min[level % dimension].compareTo(actual.getKey(level)) <= 0;
			if (goLeft && (fromRight == null || fromRight != actual.getRightSon()) &&
					(fromLeft == null || fromLeft != actual.getLeftSon())) {
				actual = actual.getLeftSon();
				++level;
			} else {
				if ((actual.getRightSon() == null || fromRight != actual.getRightSon())) {
					if (actual.keysInRange(min, max)) {
						output.add(actual.getData());
					}
				}
				boolean goRight = actual.getRightSon() != null && max[level % dimension].compareTo(actual.getKey(level)) >= 0;
				if (goRight && fromRight != actual.getRightSon()) {
					actual = actual.getRightSon();
					++level;
				} else {
					if (actual != root && !actual.isLeftSon()) {
						fromRight = actual;
					} else {
						fromLeft = actual;
					}
					actual = actual.getParent();
					--level;
				}
			}
		}
		return output;
	}

	private void delete(KDTreeNode<K, D> node, int level) throws KeysCountDoesNotMatchException {
		// node by which we will substitute the deleted node
		KDTreeNode<K, D> substitute;
		int comparingLevel = level; // level by which we compare keys
		do {
			boolean wasRight = false;
			substitute = null;
			if (node.getLeftSon() != null) {
				// if has left son we find max in left
				K maxKey = node.getLeftSon().getKey(comparingLevel);
				// initiate left sub tree
				Stack<Map.Entry<KDTreeNode<K, D>, Integer>> leftSubTree = new Stack<>();
				leftSubTree.push(new AbstractMap.SimpleEntry<>(node.getLeftSon(), comparingLevel + 1));
				substitute = node.getLeftSon();
				// level of actually found node
				int substituteLevel = comparingLevel + 1;
				// find max key
				while (leftSubTree.size() > 0) {
					Map.Entry<KDTreeNode<K, D>, Integer> entry = leftSubTree.pop();
					KDTreeNode<K, D> actual = entry.getKey();
					int actualLevel = entry.getValue();
					pushSons(actual, leftSubTree, actualLevel + 1);
					// we only accept new node if key is greater or key is equal and node is leaf
					if (actual.getKey(comparingLevel).compareTo(maxKey) >= 0 && (actual.getKey(comparingLevel).compareTo(maxKey) > 0 || actual.isLeaf())) {
						maxKey = actual.getKey(comparingLevel);
						substitute = actual;
						substituteLevel = actualLevel;
					}
				}
				// we have our substitute, now save its level
				comparingLevel = substituteLevel;
			} else if (node.getRightSon() != null) {
				wasRight = true;
				// if has right son we find max in right
				K maxKey = node.getRightSon().getKey(comparingLevel);
				// initiate left sub tree
				Stack<Map.Entry<KDTreeNode<K, D>, Integer>> rightSubTree = new Stack<>();
				rightSubTree.push(new AbstractMap.SimpleEntry<>(node.getRightSon(), comparingLevel + 1));
				substitute = node.getRightSon();
				// level of actually found node
				int substituteLevel = comparingLevel + 1;
				// find max key
				while (rightSubTree.size() > 0) {
					Map.Entry<KDTreeNode<K, D>, Integer> entry = rightSubTree.pop();
					KDTreeNode<K, D> actual = entry.getKey();
					int actualLevel = entry.getValue();
					pushSons(actual, rightSubTree, actualLevel + 1);
					// we only accept new node if key is greater or key is equal and node is leaf
					if (actual.getKey(comparingLevel).compareTo(maxKey) >= 0 && (actual.getKey(comparingLevel).compareTo(maxKey) > 0 || actual.isLeaf())) {
						maxKey = actual.getKey(comparingLevel);
						substitute = actual;
						substituteLevel = actualLevel;
					}
				}
				// we have our substitute, now save its level
				comparingLevel = substituteLevel;
			}
			// swap substitute for deleted
			KDTreeNode<K, D> parent = node.getParent();
			KDTreeNode<K, D> left = node.getLeftSon();
			KDTreeNode<K, D> right = node.getRightSon();
			// substitutes parents new son is substitute
			if (node == root) {
				root = substitute;
			} else if (node.isLeftSon()) {
				parent.setLeftSon(substitute);
			} else {
				parent.setRightSon(substitute);
			}
			// substitutes sons and parent are nodes sons
			if (substitute != null) {
				// if substitute is one of nodes son we swap them
				if (left == substitute || right == substitute) {
					// swapping parent for son
					boolean isLeftSon = substitute.isLeftSon();
					setFamily(node, substitute.getLeftSon(), substitute.getRightSon());
					setFamily(substitute, isLeftSon ? node : left, isLeftSon ? right : node);
				} else {
					boolean isLeft = substitute.isLeftSon();
					setFamily(node, substitute.getLeftSon(), substitute.getRightSon());
					KDTreeNode<K, D> subParent = substitute.getParent();
					setFamily(substitute.getParent(), isLeft ? node : subParent.getLeftSon(), isLeft ? subParent.getRightSon() : node);
					setFamily(substitute, left, right);
				}
				substitute.setParent(parent);
				if (wasRight) {
					substitute.setLeftSon(substitute.getRightSon());
					substitute.setRightSon(null);
				}
			} else {
				node.setParent(null);
				node.setRightSon(null);
				node.setLeftSon(null);
			}
		} while (substitute != null);
	}

	private void setFamily(KDTreeNode<K, D> parent, KDTreeNode<K, D> leftSon, KDTreeNode<K, D> rightSon) {
		parent.setLeftSon(leftSon);
		parent.setRightSon(rightSon);
		if (leftSon != null) {
			leftSon.setParent(parent);
		}
		if (rightSon != null) {
			rightSon.setParent(parent);
		}
	}

	private void pushSons(KDTreeNode<K, D> node, Stack<Map.Entry<KDTreeNode<K, D>, Integer>> subTree, int level) {
		if (node.getLeftSon() != null) {
			subTree.push(new AbstractMap.SimpleEntry<>(node.getLeftSon(), level));
		}
		if (node.getRightSon() != null) {
			subTree.push(new AbstractMap.SimpleEntry<>(node.getRightSon(), level));
		}
	}

	public boolean delete(K[] keys, D data) throws KeysCountDoesNotMatchException {
		KeysCountDoesNotMatchException.checkAndThrow(dimension, keys.length);
		KDTreeNode<K, D> actual = root;
		int level = 0;
		// find the node
		while (actual != null) {
			if (actual.keysEqual(keys) && actual.getData() == data) {
				// delete actual
				delete(actual, level);
				--size;
				return true;
			} else if (keys[level % dimension].compareTo(actual.getKey(level)) <= 0) {
				// go left
				actual = actual.getLeftSon();
			} else {
				// go right
				actual = actual.getRightSon();
			}
			++level;
		}
		return false;
	}

	public void levelOrder() {
		if (root == null) {
			System.out.println("Empty tree.");
		} else {
			LinkedBlockingDeque<KDTreeNode<K, D>> actualLevel = new LinkedBlockingDeque<>();
			LinkedBlockingDeque<KDTreeNode<K, D>> nextLevel = new LinkedBlockingDeque<>();
			actualLevel.add(root);
			int level = 0;
			while (actualLevel.size() > 0) {
				System.out.printf("Level : %d\n", level);
				while (actualLevel.size() > 0) {
					KDTreeNode<K, D> actualNode = actualLevel.poll();
					System.out.println(Arrays.toString(actualNode.getKeys()) + (actualNode == root ? " root" :
							((actualNode.isLeftSon() ? " left to " : " right to") + Arrays.toString(actualNode.getParent().getKeys()))));
					if (actualNode.getLeftSon() != null) {
						nextLevel.add(actualNode.getLeftSon());
					}
					if (actualNode.getRightSon() != null) {
						nextLevel.add(actualNode.getRightSon());
					}
				}
				while (nextLevel.size() > 0) {
					actualLevel.add(nextLevel.poll());
				}
				++level;
			}
		}
	}

	public void clear() {
		root = null;
		size = 0;
	}

}
