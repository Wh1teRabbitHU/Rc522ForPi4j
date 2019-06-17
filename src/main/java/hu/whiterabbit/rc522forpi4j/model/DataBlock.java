package hu.whiterabbit.rc522forpi4j.model;

import java.nio.charset.StandardCharsets;

public class DataBlock {

	private final int blockAddress;

	private final byte[] byteData;

	public DataBlock(int blockAddress, byte[] byteData) {
		this.blockAddress = blockAddress;
		this.byteData = byteData;
	}

	public int getBlockAddress() {
		return blockAddress;
	}

	public byte[] getByteData() {
		return byteData;
	}

	public String getStringData() {
		return byteData == null ? null : new String(byteData, StandardCharsets.US_ASCII);
	}
}
