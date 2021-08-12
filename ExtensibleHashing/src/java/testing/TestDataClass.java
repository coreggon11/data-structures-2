package testing;

import structures.ExtensibleHashing;

import java.io.*;
import java.util.BitSet;

public class TestDataClass implements ExtensibleHashing.Hashable<TestDataClass> {

	private long id;

	public long getId() {
		return id;
	}

	public TestDataClass(long id) {
		this.id = id;
	}

	protected TestDataClass() {
		id = 0;
	}

	public static final TestDataClass INSTANCE = new TestDataClass();

	public String toString() {
		return "" + id;
	}

	@Override
	public BitSet getHash() {
		return BitSet.valueOf(new long[]{id});
	}

	@Override
	public boolean isEqual(TestDataClass data) {
		return data.id == id;
	}

	@Override
	public TestDataClass getInstance() {
		return new TestDataClass();
	}

	@Override
	public boolean isEmpty() {
		return id == 0;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeLong(id);
		return baos.toByteArray();
	}

	@Override
	public void fromByteArray(byte[] array) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(array));
		id = dis.readLong();
	}

	@Override
	public int getSize() {
		return Long.BYTES;
	}
}
