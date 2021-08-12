package structures;

import exceptions.KeyAlreadyPresentException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ExtensibleHashing<D extends ExtensibleHashing.Hashable<D>> {

	private int fileDepth;
	private final int maxDepth;
	private int lastAddress;
	private int lastCongesting;
	private final int blockFactor;
	private final RandomAccessFile file;
	private final RandomAccessFile congestingFile;
	private int[] directory;
	private Stack<Integer> freeAddresses;
	private Stack<Integer> freeCongAddresses;
	private final String fileName;
	private final D instance;

	@SuppressWarnings("unchecked")
	public ExtensibleHashing(String fileName, int maxDepth, int blockFactor, D instance) throws IOException {
		this.blockFactor = blockFactor;
		file = new RandomAccessFile(fileName, "rw");
		congestingFile = new RandomAccessFile("cong." + fileName, "rw");
		this.fileName = fileName;
		this.maxDepth = maxDepth;
		directory = new int[2];
		this.instance = instance;
		file.seek(0);
		try {
			fileDepth = file.readInt();
			FileInputStream fis = new FileInputStream(getDirName());
			ObjectInputStream ois = new ObjectInputStream(fis);
			lastAddress = (Integer) ois.readObject();
			lastCongesting = (Integer) ois.readObject();
			directory = (int[]) ois.readObject();
			freeAddresses = (Stack<Integer>) ois.readObject();
			freeCongAddresses = (Stack<Integer>) ois.readObject();
			ois.close();
			fis.close();
		} catch (EOFException | ClassNotFoundException | FileNotFoundException e) {
			//if we reach EOF there is nothing written yet - we are creating a new file
			fileDepth = 1;
			int blockSize = new Block<D>(blockFactor, instance).getSize();
			lastAddress = initialAddress() + blockSize;
			lastCongesting = 0;
			file.seek(0);
			file.writeInt(fileDepth);
			file.write(new Block<>(blockFactor, instance, 0).toByteArray());
			file.write(new Block<>(blockFactor, instance, 1).toByteArray());
			directory[0] = initialAddress();
			directory[1] = blockSize + initialAddress();
			freeAddresses = new Stack<>();
			freeCongAddresses = new Stack<>();
		}
	}

	public void clear() throws IOException {
		file.setLength(0);
		file.seek(0);
		file.writeInt(fileDepth);
		file.write(new Block<>(blockFactor, instance, 0).toByteArray());
		file.write(new Block<>(blockFactor, instance, 1).toByteArray());
		congestingFile.setLength(0);
		fileDepth = 1;
		int blockSize = new Block<D>(blockFactor, instance).getSize();
		lastAddress = initialAddress() + blockSize;
		lastCongesting = 0;
		directory = new int[2];
		directory[0] = initialAddress();
		directory[1] = blockSize + initialAddress();
		freeAddresses.clear();
		freeCongAddresses.clear();
	}

	/**
	 * closes the file and saves the directory to file
	 */
	public void close() throws IOException {
		file.close();
		congestingFile.close();
		FileOutputStream fos = new FileOutputStream(getDirName());
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(lastAddress);
		oos.writeObject(lastCongesting);
		oos.writeObject(directory);
		oos.writeObject(freeAddresses);
		oos.writeObject(freeCongAddresses);
		oos.close();
		fos.close();
	}

	private String getDirName() {
		return "dir." + fileName;
	}

	private int initialAddress() {
		return Integer.BYTES;// file depth is first
	}

	public D find(D data) throws IOException {
		BitSet hash = data.getHash();
		int index = hashToIndex(hash, fileDepth);
		int address = directory[index];
		Block<D> block = new Block<>(blockFactor, data);
		byte[] blockBytes = new byte[block.getSize()];
		file.seek(address);
		file.read(blockBytes);
		block.fromByteArray(blockBytes);
		D out = block.find(data);
		while (out == null && block.hasNext()) {
			int nextAddress = block.nextBlockAddress;
			congestingFile.seek(nextAddress);
			congestingFile.read(blockBytes);
			block.fromByteArray(blockBytes);
			out = block.find(data);
		}
		return out;
	}

	public void update(D originalData, D newData) throws IOException, KeyAlreadyPresentException {
		BitSet hash = originalData.getHash();
		int index = hashToIndex(hash, fileDepth);
		int address = directory[index];
		Block<D> block = new Block<>(blockFactor, originalData);
		byte[] blockBytes = new byte[block.getSize()];
		file.seek(address);
		file.read(blockBytes);
		block.fromByteArray(blockBytes);
		D out = block.find(originalData);
		boolean inCongesting = false;
		while (out == null && block.hasNext()) {
			address = block.nextBlockAddress;
			congestingFile.seek(address);
			congestingFile.read(blockBytes);
			block.fromByteArray(blockBytes);
			inCongesting = true;
			out = block.find(originalData);
		}
		if (out != null) {
			if (out.isEqual(newData)) {
				// if same primary keys
				block.update(out, newData);
				if (inCongesting) {
					congestingFile.seek(address);
					congestingFile.write(block.toByteArray());
				} else {
					file.seek(address);
					file.write(block.toByteArray());
				}
			} else if (find(newData) == null) {
				delete(out);
				insert(newData);
			} else {
				throw KeyAlreadyPresentException.EXCEPTION;
			}
		}
	}

	public void insert(D data) throws IOException, KeyAlreadyPresentException {
		boolean inserted = false;
		while (!inserted) {
			BitSet hash = data.getHash();
			// we want first depth 1s
			// address index in directory
			int index = hashToIndex(hash, fileDepth);
			Block<D> block = new Block<>(blockFactor, data);
			byte[] blockBytes = new byte[block.getSize()];
			int address = directory[index];
			file.seek(address);
			file.read(blockBytes);
			// init block
			block.fromByteArray(blockBytes);
			// check for the key
			if (block.containsData(data)) {
				throw KeyAlreadyPresentException.EXCEPTION;
			} else if (block.hasNext()) {
				boolean repeat = true;
				int nextAddress = block.nextBlockAddress;
				Block<D> congesting = new Block<>(blockFactor, data);
				while (repeat) {
					congestingFile.seek(nextAddress);
					congestingFile.read(blockBytes);
					congesting.fromByteArray(blockBytes);
					if (congesting.containsData(data)) {
						throw KeyAlreadyPresentException.EXCEPTION;
					} else if (congesting.hasNext()) {
						nextAddress = congesting.nextBlockAddress;
					} else {
						repeat = false;
					}
				}
			}
			if (block.isFull()) {
				if (block.blockDepth < maxDepth) {
					if (block.blockDepth == fileDepth) {
						// double directory
						int[] newDirectory = new int[directory.length * 2];
						for (int i = 0; i < directory.length; ++i) {
							newDirectory[i * 2] = directory[i];
							newDirectory[(i * 2) + 1] = directory[i];
						}
						directory = newDirectory;
						++fileDepth;
						// write file depth
						file.seek(0);
						file.writeInt(fileDepth);
						index *= 2;
					}
					int newAddress;
					if (freeAddresses.isEmpty()) {
						// split
						lastAddress += block.getSize();
						newAddress = lastAddress;
					} else {
						newAddress = freeAddresses.pop();
					}
					Block<D> newBlock = block.split();
					// we need to update where the index is pointing
					int updates = (1 << (fileDepth - block.blockDepth + 1)) / 2;
					int originalIndex = (index >>> (fileDepth - block.blockDepth + 1)) << (fileDepth - block.blockDepth + 1);
					for (int i = 0; i < updates; ++i) {
						directory[originalIndex + i + updates] = newAddress;
					}
					// write blocks
					file.seek(address);
					file.write(block.toByteArray());
					file.seek(newAddress);
					file.write(newBlock.toByteArray());
				} else {
					// we must use congesting
					boolean inCongesting = false;
					int nextAddress = block.nextBlockAddress;
					while (!inserted) {
						if (block.hasNext()) {
							// load next
							nextAddress = block.nextBlockAddress;
							inCongesting = true;
							congestingFile.seek(nextAddress);
							congestingFile.read(blockBytes);
							block.fromByteArray(blockBytes);
							if (!block.isFull()) {
								block.insert(data);
								congestingFile.seek(nextAddress);
								congestingFile.write(block.toByteArray());
								inserted = true;
							}
						} else {
							if (freeCongAddresses.isEmpty()) {
								block.nextBlockAddress = lastCongesting;
								lastCongesting += block.getSize();
							} else {
								block.nextBlockAddress = freeCongAddresses.pop();
							}
							if (!inCongesting) {
								file.seek(address);
								file.write(block.toByteArray());
							} else {
								congestingFile.seek(nextAddress);
								congestingFile.write(block.toByteArray());
							}
							Block<D> newBlock = new Block<>(blockFactor, data);
							newBlock.insert(data);
							congestingFile.seek(block.nextBlockAddress);
							congestingFile.write(newBlock.toByteArray());
							inserted = true;
						}
					}
				}
			} else {
				// insert
				block.insert(data);
				file.seek(address);
				file.write(block.toByteArray());
				inserted = true;
			}
		}
	}

	public static int hashToIndex(BitSet hash, int depth) {
		return Integer.reverse(fromBitSet(hash.get(0, depth))) >>> (Integer.SIZE - depth);
	}

	private static int fromBitSet(BitSet bitSet) {
		int output = 0;
		for (int i = 0; i < bitSet.length(); ++i) {
			output += bitSet.get(i) ? 1 << i : 0;
		}
		return output;
	}

	public D delete(D data) throws IOException {
		// get index of address
		BitSet hash = data.getHash();
		int index = hashToIndex(hash, fileDepth);
		// get address
		int address = directory[index];
		// get block
		Block<D> block = new Block<>(blockFactor, data);
		byte[] bytes = new byte[block.getSize()];
		file.seek(address);
		file.read(bytes);
		block.fromByteArray(bytes);
		D deleted = block.delete(data);
		boolean deleteFromCongesting = false;
		if (deleted != null) {
			file.seek(address);
			file.write(block.toByteArray());
		} else if (block.hasNext()) {
			// try to delete from congesting
			int nextAddress = block.nextBlockAddress;
			while (deleted == null && block.hasNext()) {
				congestingFile.seek(nextAddress);
				congestingFile.read(bytes);
				block.fromByteArray(bytes);
				deleted = block.delete(data);
				if (deleted != null) {
					// was deleted from congesting
					congestingFile.seek(nextAddress);
					congestingFile.write(block.toByteArray());
					address = nextAddress;
					deleteFromCongesting = true;
				} else if (block.hasNext()) {
					nextAddress = block.nextBlockAddress;
				}
			}
		}
		// first we need to move from congesting to block
		while (deleted != null && block.hasNext()) {
			congestingFile.seek(block.nextBlockAddress);
			congestingFile.read(bytes);
			Block<D> congesting = new Block<>(blockFactor, data);
			congesting.fromByteArray(bytes);
			if (congesting.validRecords() + block.validRecords() <= blockFactor) {
				if (block.nextBlockAddress == lastCongesting) {
					lastCongesting -= block.getSize();
				} else {
					freeCongAddresses.push(block.nextBlockAddress);
				}
				block.mergeWithCongesting(congesting);
				if (deleteFromCongesting) {
					congestingFile.seek(address);
					congestingFile.write(block.toByteArray());
				} else {
					file.seek(address);
					file.write(block.toByteArray());
				}
			} else {
				break;
			}
		}
		boolean repeat = deleted != null;
		while (!deleteFromCongesting && repeat && block.blockDepth > 1) {
			repeat = false;
			// get neighbor block
			// search right
			int rightIndex = -1;
			for (int i = index + 1; i < directory.length; ++i) {
				if (directory[i] != address) {
					rightIndex = i;
					break;
				}
			}
			// search left
			int leftIndex = -1;
			for (int i = index - 1; i >= 0; --i) {
				if (directory[i] != address) {
					leftIndex = i;
					break;
				}
			}
			if (leftIndex != -1 || rightIndex != -1) {
				// a neighbor maybe exists
				Block<D> neighbor = null;
				if (leftIndex != -1) {
					neighbor = new Block<>(blockFactor, data);
					loadBlock(neighbor, leftIndex);
					if (!block.isNeighbor(neighbor)) {
						neighbor = null;
					} else {
						rightIndex = -1;
					}
				}
				if (neighbor == null && rightIndex != -1) {
					neighbor = new Block<>(blockFactor, data);
					loadBlock(neighbor, rightIndex);
					if (!block.isNeighbor(neighbor)) {
						neighbor = null;
					} else {
						leftIndex = -1;
					}
				}
				// if neighbor exists we try to merge them
				if (neighbor != null && !block.hasNext() && !neighbor.hasNext()) {
					int records = block.validRecords() + neighbor.validRecords();
					if (records <= blockFactor) {
						// we merge blocks
						block.merge(neighbor);
						repeat = true;
						// if from left
						int newAddress = leftIndex != -1 ? directory[leftIndex] : directory[rightIndex - 1];
						address = newAddress;
						file.seek(newAddress);
						file.write(block.toByteArray());
						int oldAddress;
						if (leftIndex != -1) {
							oldAddress = directory[leftIndex + 1];
							for (int i = leftIndex + 1; i < directory.length; ++i) {
								if (directory[i] == oldAddress) {
									directory[i] = directory[leftIndex];
								} else {
									break;
								}
							}
						} else {
							oldAddress = directory[rightIndex];
							for (int i = rightIndex; i < directory.length; ++i) {
								if (directory[i] == oldAddress) {
									directory[i] = directory[rightIndex - 1];
								} else {
									break;
								}
							}
						}
						// if old address is less than max address we add it to stack
						if (oldAddress < lastAddress) {
							freeAddresses.push(oldAddress);
						} else {
							lastAddress -= block.getSize();
						}
						// now find out if last block with that depth
						int singlePointers = 0;
						int counter = 0;
						int last = -1;
						for (int addressNow : directory) {
							if (addressNow == last) {
								counter++;
							} else if (last != -1) {
								if (counter == 0) {
									singlePointers++;
									break;
								}
								counter = 0;
							}
							last = addressNow;
						}
						// if we did not find blocks which are only on one address
						if (singlePointers == 0) {
							int[] newDirectory = new int[directory.length / 2];
							for (int i = 0; i < newDirectory.length; ++i) {
								newDirectory[i] = directory[i * 2];
							}
							directory = newDirectory;
							--fileDepth;
							index /= 2;
							file.seek(0);
							file.write(fileDepth);
						}
					}
				}
			}
		}
		return deleted;
	}

	public void print() throws IOException {
		int lastAddress = -1;
		for (int address : directory) {
			if (address != lastAddress) {
				System.out.printf("Block %d [", address);
				Block<D> block = new Block<>(blockFactor, instance);
				byte[] bytes = new byte[block.getSize()];
				file.seek(address);
				file.read(bytes);
				block.fromByteArray(bytes);
				System.out.printf("%s], D:%d", block.records.stream().map(D::toString).collect(Collectors.joining(",")), block.blockDepth);
				while (block.hasNext()) {
					congestingFile.seek(block.nextBlockAddress);
					congestingFile.read(bytes);
					block.fromByteArray(bytes);
					System.out.printf(" -> [%s]", block.records.stream().map(D::toString).collect(Collectors.joining(",")));
				}
				System.out.printf("%n");
				lastAddress = address;
			}
		}
	}

	public List<Block<D>> getAllBlocks() throws IOException {
		int lastAddress = -1;
		int size = 0;
		for (int address : directory) {
			if (address != lastAddress) {
				++size;
				lastAddress = address;
			}
		}
		ArrayList<Block<D>> out = new ArrayList<>(size);
		for (int address : directory) {
			if (address != lastAddress) {
				Block<D> block = new Block<>(blockFactor, instance);
				byte[] bytes = new byte[block.getSize()];
				file.seek(address);
				file.read(bytes);
				block.fromByteArray(bytes);
				out.add(block);
				block.address = address;
				while (block.hasNext()) {
					Block<D> old = block;
					congestingFile.seek(block.nextBlockAddress);
					congestingFile.read(bytes);
					block = new Block<>(blockFactor, instance);
					block.fromByteArray(bytes);
					old.next = block;
				}
				lastAddress = address;
			}
		}
		return out;
	}

	private void loadBlock(Block<D> block, int index) throws IOException {
		byte[] bytes = new byte[block.getSize()];
		file.seek(directory[index]);
		file.read(bytes);
		block.fromByteArray(bytes);
	}

	public static class Block<D extends Hashable<D>> {
		private final int blockFactor;
		private final ArrayList<D> records;
		private final int objectSize;
		private int nextBlockAddress;
		private int blockDepth;
		private long prefix;
		// just for testing purposes
		private Block<D> next;
		private int address;

		/**
		 * constructor
		 *
		 * @param blockFactor maximum records of block
		 * @param instance    instance of class to be contained in the block (and the structure)
		 */
		public Block(int blockFactor, D instance, long prefix) {
			records = new ArrayList<>(blockFactor);
			for (int i = 0; i < blockFactor; ++i) {
				records.add(instance.getInstance());
			}
			objectSize = records.get(0).getSize();
			this.blockFactor = blockFactor;
			this.blockDepth = 1;
			this.prefix = prefix;
			this.nextBlockAddress = -1;
		}

		public Block(int blockFactor, D instance) {
			this(blockFactor, instance, 0);
		}

		public Block<D> getNext() {
			return next;
		}

		public int getAddress() {
			return address;
		}

		public List<D> getValidRecords() {
			return records.stream().filter(data -> !data.isEmpty()).collect(Collectors.toList());
		}

		/**
		 * inserts data into block
		 *
		 * @param data data to be inserted
		 */
		public void insert(D data) {
			for (int i = 0; i < blockFactor; ++i) {
				if (records.get(i).isEmpty()) {
					records.set(i, data);
					break;
				}
			}
		}

		/**
		 * @return true if the block is full
		 */
		public boolean isFull() {
			for (D instance : records) {
				if (instance.isEmpty()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * splits the block to two, increases the depth. records with hash matching hash of block with new depth are kept in block,
		 * the rest are added to new block
		 *
		 * @return block created after split with records with hash matching the hash of new block
		 */
		public Block<D> split() {
			D instance = records.get(0).getInstance();
			Block<D> newBlock = new Block<>(blockFactor, instance);
			newBlock.prefix = prefix + (1 << blockDepth);
			blockDepth++;
			int added = 0;
			for (int i = 0; i < records.size(); ++i) {
				// if fits hash1 - stays
				int hashNow = Integer.reverse(hashToIndex(records.get(i).getHash(), blockDepth)) >>> (Integer.SIZE - blockDepth);
				if (hashNow == newBlock.prefix) {
					newBlock.records.set(added++, records.get(i));
					records.set(i, instance.getInstance());
				}
			}
			newBlock.blockDepth = blockDepth;
			return newBlock;
		}

		/**
		 * merges two blocks
		 *
		 * @param other block to be merged with this one
		 */
		public void merge(Block<D> other) {
			for (D record : other.records) {
				if (!record.isEmpty()) {
					insert(record);
				}
			}
			blockDepth--;
			prefix <<= (Long.SIZE - blockDepth);
			prefix >>>= (Long.SIZE - blockDepth);
		}

		/**
		 * merges this block with its congesting block
		 *
		 * @param congesting congesting block of this block
		 */
		public void mergeWithCongesting(Block<D> congesting) {
			for (D record : congesting.records) {
				if (!record.isEmpty()) {
					insert(record);
				}
			}
			nextBlockAddress = congesting.hasNext() ? congesting.nextBlockAddress : -1;
		}

		/**
		 * @return the number of non empty records
		 */
		public int validRecords() {
			return (int) records.stream().filter(data -> !data.isEmpty()).count();
		}

		/**
		 * finds data with same primary keys as set in the instance passed in argument
		 *
		 * @return data from the block with same primary keys (same hash)
		 */
		public D find(D data) {
			return records.stream().filter(d -> d.isEqual(data)).findFirst().orElse(null);
		}

		public void update(D original, D updated) {
			for (int i = 0; i < records.size(); ++i) {
				if (records.get(i).isEqual(original)) {
					records.set(i, updated);
				}
			}
		}

		/**
		 * converts the block to byte array
		 *
		 * @return byte representation of the block
		 */
		public byte[] toByteArray() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(blockDepth);
			dos.writeInt(nextBlockAddress);
			dos.writeLong(prefix);
			for (D record : records) {
				for (Byte b : record.toByteArray()) {
					dos.write(b);
				}
			}
			return baos.toByteArray();
		}

		/**
		 * loads block's data from byte array
		 *
		 * @param bytes bytes representing the block
		 */
		public void fromByteArray(byte[] bytes) throws IOException {
			for (int i = 0; i < blockFactor; ++i) {
				byte[] dataBytes = Arrays.copyOfRange(bytes, i * records.get(i).getSize() + Integer.BYTES * 2 + Long.BYTES,
						(i + 1) * records.get(i).getSize() + Integer.BYTES * 2 + Long.BYTES);
				records.get(i).fromByteArray(dataBytes);
			}
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
			blockDepth = dis.readInt();
			nextBlockAddress = dis.readInt();
			prefix = dis.readLong();
		}

		public boolean isNeighbor(Block<D> other) {
			return blockDepth == other.blockDepth && ((prefix << (Long.SIZE - blockDepth + 1)) == (other.prefix << (Long.SIZE - other.blockDepth + 1)));
		}

		/**
		 * @param data object with primary keys set
		 * @return true if data with same primary keys is present in block
		 */
		public boolean containsData(D data) {
			for (D record : records) {
				if (record.isEqual(data)) {
					return true;
				}
			}
			return false;
		}

		public boolean hasNext() {
			return nextBlockAddress != -1;
		}

		/**
		 * deletes record with same primary keys as data sent by parameter
		 *
		 * @param data instance with same primary keys
		 * @return deleted record
		 */
		public D delete(D data) {
			for (int i = 0; i < blockFactor; ++i) {
				D record = records.get(i);
				if (record.isEqual(data)) {
					records.set(i, record.getInstance());
					return record;
				}
			}
			return null;
		}

		/**
		 * @return size of block in bytes
		 */
		public int getSize() {
			return blockFactor * objectSize + Integer.BYTES * 2 + Long.BYTES;
		}
	}

	public interface Hashable<D> {
		/**
		 * @return primary keys of the object represented in bits
		 */
		BitSet getHash();

		/**
		 * @param data data to be compared
		 * @return true if the instances have equal keys
		 */
		boolean isEqual(D data);

		/**
		 * @return not initialized (empty) instance of the class
		 */
		D getInstance();

		/**
		 * @return true if the object is not initialized (empty)
		 * key attributes should be empty when equal to 0 ("" in case of string etc.)
		 */
		boolean isEmpty();

		/**
		 * @return bytes representing the object
		 */
		byte[] toByteArray() throws IOException;

		/**
		 * @param array bytes representing the object
		 */
		void fromByteArray(byte[] array) throws IOException;

		/**
		 * @return size of the object in BYTES
		 */
		int getSize();
	}

}
