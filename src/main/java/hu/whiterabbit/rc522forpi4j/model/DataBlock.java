package hu.whiterabbit.rc522forpi4j.model;

import java.nio.charset.StandardCharsets;

public class DataBlock {

	private final int blockAddress;

	private final byte[] data;

	public DataBlock(int blockAddress, byte[] data) {
		this.blockAddress = blockAddress;
		this.data = data;
	}

	public int getBlockAddress() {
		return blockAddress;
	}

	public byte[] getByteData() {
		return data;
	}

	public String getStringData() {
		return data == null ? null : new String(data, StandardCharsets.US_ASCII);
	}

	@Override
	public String toString() {
		return "DataBlock{" +
				"blockAddress=" + blockAddress +
				", byteData=" + getStringData() +
				'}';
	}
}
