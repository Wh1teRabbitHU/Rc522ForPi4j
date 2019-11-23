package hu.whiterabbit.rc522forpi4j.model.card;

import hu.whiterabbit.rc522forpi4j.util.CardUtil;
import hu.whiterabbit.rc522forpi4j.util.DataUtil;

import java.nio.charset.StandardCharsets;

import static hu.whiterabbit.rc522forpi4j.model.card.Sector.MAX_SECTOR_SIZE;

public class Block {

	private static final int MAX_BLOCK_SIZE = 16;

	static final int SECTOR_TRAILER_BLOCK_INDEX = 3;

	static final int MANUFACTURER_BLOCK_INDEX = 0;

	static final int MANUFACTURER_SECTOR_INDEX = 0;

	private final int number;

	private final byte[] data;

	private BlockType blockType;

	private BlockAccessMode accessMode;

	public Block(int sectorIndex, int blockIndex) {
		this(sectorIndex, blockIndex, null);
	}

	public Block(int sectorIndex, int blockIndex, byte[] data) {
		if (blockIndex < 0 || blockIndex >= MAX_SECTOR_SIZE) {
			throw new RuntimeException("Given block index is out of range! (" + blockIndex + ")");
		} else if (data != null && data.length > MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given data array is too large! (" + data.length +
					") It should be less than " + MAX_BLOCK_SIZE);
		}

		this.number = blockIndex;

		if (data == null) {
			this.data = new byte[MAX_BLOCK_SIZE];
		} else {
			this.data = data;
		}

		if (blockIndex == SECTOR_TRAILER_BLOCK_INDEX) {
			blockType = BlockType.SECTOR_TRAILER;
		} else if (blockIndex == MANUFACTURER_BLOCK_INDEX && sectorIndex == MANUFACTURER_SECTOR_INDEX) {
			blockType = BlockType.MANUFACTURER;
		} else {
			blockType = BlockType.DATA;
		}
	}

	public int getNumber() {
		return number;
	}

	public byte getByte(int byteIndex) {
		if (byteIndex < 0 || byteIndex >= MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given byte number is out of range! (" + byteIndex + ")");
		}

		return data[byteIndex];
	}

	public byte[] getByteRange(int startingIndex, int number) {
		byte[] result = new byte[number];

		for (int i = 0; i < number; i++) {
			result[i] = getByte(startingIndex + i);
		}

		return result;
	}

	public void setByte(int byteIndex, byte value) {
		if (byteIndex < 0 || byteIndex >= MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given byte number is out of range! (" + byteIndex + ")");
		}

		data[byteIndex] = value;
	}

	public byte[] getData() {
		return data;
	}

	public String getDataAsHex() {
		return DataUtil.bytesToHex(data, (a, b) -> a + ", " + b);
	}

	public String getDataAsString() {
		return data == null ? null : new String(data, StandardCharsets.US_ASCII);
	}

	public BlockType getBlockType() {
		return blockType;
	}

	public void setBlockType(BlockType blockType) {
		this.blockType = blockType;
	}

	public BlockAccessMode getAccessMode() {
		return accessMode;
	}

	public void updateAccessMode(Block sectorTrailerBlock) {
		byte[] accessBytes = sectorTrailerBlock.getByteRange(6, 3);

		this.accessMode = CardUtil.getBlockAccessMode(this, accessBytes);
	}

	@Override
	public String toString() {
		int blockTypeLength = blockType.toString().length();
		int maxBlockTypeLength = BlockType.SECTOR_TRAILER.toString().length();
		int extraSpaceNeeded = maxBlockTypeLength - blockTypeLength;

		String blockPart = "[" + blockType + "]" + new String(new char[extraSpaceNeeded]).replace("\0", " ");

		return "\tBlock (" + number + ") " + blockPart + "\t" + getDataAsHex();
	}
}
