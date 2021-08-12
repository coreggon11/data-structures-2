package testing;

import application.business.Property;
import exceptions.KeyAlreadyPresentException;
import structures.ExtensibleHashing;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Test {

	public static void main(String[] args) {
//		fixedSeedTest();
//		randomSeedTest();
		randomPropertyTest();
	}

	private static void randomSeedTest() {
		randomTest(-1, false, true, -1, false);
	}

	private static void randomPropertyTest() {
		propertyTest(-1, false, true, -1, false);
	}

	private static void fixedSeedTest() {
		randomTest(1607352609, false, true, -1, false);
	}

	private static void seedWithLeastOpsTest() {
		long bestSeed = 1607291129;
		long seed = System.currentTimeMillis() / 1000;
		boolean printOperations = false;
		int leastOps = 1000;
		int replications = 1000000;
		for (int i = 0; i < replications; ++i) {
			int operations = randomTest(seed, false, true, leastOps, false);
			if (operations < leastOps) {
				leastOps = operations;
				bestSeed = seed;
			}
			seed++;
			System.out.println("Best seed: " + bestSeed + ", operations: " + leastOps);
		}
	}

	public static int randomTest(long seed, boolean printOperations, boolean breakAfterWrong, int maxOperations, boolean printData) {
		try {
			seed = seed == -1 ? System.currentTimeMillis() / 1000 : seed;
			ExtensibleHashing<TestDataClass> hashing = new ExtensibleHashing<>("testing" + seed + ".bin", 16, 4, TestDataClass.INSTANCE);
			int operationsCount = maxOperations == -1 ? 10000000 : maxOperations;
			ArrayList<Integer> insertedKeys = new ArrayList<>();
			Random r = new Random(seed);
			System.out.println("Seed: " + seed);

			final int SAME_INSERT = 0;
			final int INSERT = 1;
			final int FIND = 2;
			final int FIND_NON_EXISTING = 3;
			final int DELETE = 4;
			final int DELETE_NON_EXISTING = 5;

			int size = 0;
			int wrong = 0;
			int percent = 0;

			for (int i = 1; i <= operationsCount; ++i) {
				if (wrong > 0 && breakAfterWrong) {
					return i;
				}
				if (i * 100d / operationsCount >= percent) {
					System.out.println(percent + " % operations done");
					percent++;
				}
				int operation = r.nextInt(6);
				switch (operation) {
					case SAME_INSERT:
						if (size != 0) {
							// if we have not inserted anything we skip
							int inserted = insertedKeys.get(r.nextInt(insertedKeys.size()));
							try {
								hashing.insert(new TestDataClass(inserted));
								// should not continue
								System.out.println("Inserted same data " + inserted);
								wrong++;
							} catch (KeyAlreadyPresentException ignored) {
							} catch (EOFException e) {
								wrong++;
								System.out.println("EOF: data " + inserted);
							}
						}
						break;
					case INSERT:
						if (printOperations) {
							System.out.println("Insert " + i);
						}
						try {
							hashing.insert(new TestDataClass(i));
							insertedKeys.add(i);
							size++;
							if (printData) {
								hashing.print();
								System.out.println();
							}
						} catch (KeyAlreadyPresentException e) {
							// should not happen
							wrong++;
							System.out.println("Key present" + i);
						} catch (EOFException e) {
							wrong++;
							System.out.println("EOF: data" + i);
						}
						break;
					case FIND:
						if (size != 0) {
							// if we have not inserted anything we skip
							int lookingFor = insertedKeys.get(r.nextInt(insertedKeys.size()));
							TestDataClass find = hashing.find(new TestDataClass(lookingFor));
							if (find == null) {
								// data not found - wrong
								System.out.println("Did not find " + lookingFor);
								wrong++;
							}
						}
						break;
					case FIND_NON_EXISTING:
						if (size != 0) {
							// if we have not inserted anything we skip
							TestDataClass find = hashing.find(new TestDataClass(i));
							if (find != null) {
								// data found - wrong
								System.out.println("Found data " + i);
								wrong++;
							}
						}
						break;
					case DELETE:
						if (size != 0) {
							// if we have not inserted anything we skip
							int index = r.nextInt(insertedKeys.size());
							int lookingFor = insertedKeys.get(index);
							if (printOperations) {
								System.out.println("Delete " + lookingFor);
							}
							--size;
							insertedKeys.remove(index);
							TestDataClass delete = hashing.delete(new TestDataClass(lookingFor));
							if (delete == null) {
								// wrong
								System.out.println("Did not delete " + lookingFor);
								wrong++;
							}
							if (printData) {
								hashing.print();
								System.out.println();
							}
						}
						break;
					case DELETE_NON_EXISTING:
						if (size != 0) {
							// if we have not inserted anything we skip
							TestDataClass delete = hashing.delete(new TestDataClass(i));
							if (delete != null) {
								// data found - wrong
								System.out.println("Deleted " + i);
								wrong++;
							}
						}
					default:
						break;
				}
			}
			System.out.printf("Testing finished, %f percent of operations failed%n", (double) wrong * 100 / operationsCount);
			return operationsCount;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int propertyTest(long seed, boolean printOperations, boolean breakAfterWrong, int maxOperations, boolean printData) {
		try {
			seed = seed == -1 ? System.currentTimeMillis() / 1000 : seed;
			ExtensibleHashing<Property> hashing = new ExtensibleHashing<>("testing" + seed + ".bin", 16, 8, Property.INSTANCE);
			int operationsCount = maxOperations == -1 ? 10000000 : maxOperations;
			ArrayList<Property> insertedKeys = new ArrayList<>();
			Random r = new Random(seed);
			System.out.println("Seed: " + seed);

			final int SAME_INSERT = 0;
			final int INSERT = 1;
			final int FIND = 2;
			final int FIND_NON_EXISTING = 3;
			final int DELETE = 4;
			final int DELETE_NON_EXISTING = 5;
			final int UPDATE = 6;
			final int UPDATE_NON_EXISTING = 7;

			final int BOUND = 8;

			int size = 0;
			int wrong = 0;
			int percent = 0;

			for (int i = 1; i <= operationsCount; ++i) {
				if (wrong > 0 && breakAfterWrong) {
					return i;
				}
				if (i * 100d / operationsCount >= percent) {
					System.out.println(percent + " % operations done");
					percent++;
				}
				int operation = r.nextInt(BOUND);
				switch (operation) {
					case SAME_INSERT:
						if (size != 0) {
							// if we have not inserted anything we skip
							Property inserted = insertedKeys.get(r.nextInt(insertedKeys.size()));
							try {
								hashing.insert(inserted);
								// should not continue
								System.out.println("Inserted same data " + inserted);
								wrong++;
							} catch (KeyAlreadyPresentException ignored) {
							} catch (EOFException e) {
								wrong++;
								System.out.println("EOF: data " + inserted);
							}
						}
						break;
					case INSERT:
						if (printOperations) {
							System.out.println("Insert " + i);
						}
						try {
							Property in = randomProperty(i, r);
							hashing.insert(in);
							insertedKeys.add(in);
							size++;
							if (printData) {
								hashing.print();
								System.out.println();
							}
						} catch (KeyAlreadyPresentException e) {
							// should not happen
							wrong++;
							System.out.println("Key present" + i);
						} catch (EOFException e) {
							wrong++;
							System.out.println("EOF: data" + i);
						}
						break;
					case FIND:
						if (size != 0) {
							// if we have not inserted anything we skip
							Property lookingFor = insertedKeys.get(r.nextInt(insertedKeys.size()));
							Property find = hashing.find(lookingFor);
							if (find == null) {
								// data not found - wrong
								System.out.println("Did not find " + lookingFor);
								wrong++;
							} else if (!find.equals(lookingFor)) {
								System.out.println("Found, but wrong data! Expected " + lookingFor + ", found " + find);
								wrong++;
							}
						}
						break;
					case FIND_NON_EXISTING:
						if (size != 0) {
							// if we have not inserted anything we skip
							Property find = hashing.find(new Property(i));
							if (find != null) {
								// data found - wrong
								System.out.println("Found data " + i);
								wrong++;
							}
						}
						break;
					case DELETE:
						if (size != 0) {
							// if we have not inserted anything we skip
							int index = r.nextInt(insertedKeys.size());
							Property lookingFor = insertedKeys.get(index);
							if (printOperations) {
								System.out.println("Delete " + lookingFor);
							}
							--size;
							insertedKeys.remove(index);
							Property delete = hashing.delete(lookingFor);
							if (delete == null) {
								// wrong
								System.out.println("Did not delete " + lookingFor);
								wrong++;
							}
							if (printData) {
								hashing.print();
								System.out.println();
							}
						}
						break;
					case DELETE_NON_EXISTING:
						if (size != 0) {
							// if we have not inserted anything we skip
							Property delete = hashing.delete(new Property(i));
							if (delete != null) {
								// data found - wrong
								System.out.println("Deleted " + i);
								wrong++;
							}
						}
						break;
					case UPDATE:
						if (size != 0) {
							// if we have not inserted anything we skip
							int index = r.nextInt(insertedKeys.size());
							Property lookingFor = insertedKeys.get(index);
							if (printOperations) {
								System.out.println("Delete " + lookingFor);
							}
							Property newProperty = randomProperty(i, r);
							insertedKeys.set(index, newProperty);
							try {
								hashing.update(lookingFor, newProperty);
								Property found = hashing.find(newProperty);
								if (found == null) {
									++wrong;
									System.out.println("Could not find updated " + newProperty);
								}
							} catch (KeyAlreadyPresentException e) {
								++wrong;
								System.out.println("Could not update " + lookingFor + " with " + newProperty);
							}
						}
						break;
					case UPDATE_NON_EXISTING:
						if (size != 0) {
							// if we have not inserted anything we skip
							try {
								hashing.update(new Property(i), new Property(i));
							} catch (KeyAlreadyPresentException e) {
								++wrong;
								System.out.println("Updated non existing " + new Property(i));
							}
						}
						break;
					default:
						break;
				}
			}
			System.out.printf("Testing finished, %f percent of operations failed%n", (double) wrong * 100 / operationsCount);
			return operationsCount;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
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
