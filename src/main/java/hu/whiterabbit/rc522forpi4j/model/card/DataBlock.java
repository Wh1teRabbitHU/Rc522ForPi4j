package hu.whiterabbit.rc522forpi4j.model.card;

import hu.whiterabbit.rc522forpi4j.util.CardUtil;
import hu.whiterabbit.rc522forpi4j.util.DataUtil;

import java.nio.charset.StandardCharsets;

import static hu.whiterabbit.rc522forpi4j.model.card.Sector.MAX_SECTOR_SIZE;
import static hu.whiterabbit.rc522forpi4j.util.CardUtil.blockTypeToString;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class DataBlock {

	static final int MAX_BLOCK_SIZE = 16;

	static final int SECTOR_TRAILER_BLOCK_INDEX = 3;

	static final int MANUFACTURER_BLOCK_INDEX = 0;

	static final int MANUFACTURER_SECTOR_INDEX = 0;

	private final int index;

	private final byte[] data;

	private BlockType blockType;

	private BlockAccessMode accessMode;

	public DataBlock(int sectorIndex, int blockIndex) {
		this(sectorIndex, blockIndex, null);
	}

	public DataBlock(int sectorIndex, int blockIndex, byte[] data) {
		if (blockIndex < 0 || blockIndex >= MAX_SECTOR_SIZE) {
			throw new RuntimeException("Given block index is out of range! (" + blockIndex + ")");
		} else if (data != null && data.length > MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given data array is too large! (" + data.length +
					") It should be less than " + MAX_BLOCK_SIZE);
		}

		this.index = blockIndex;

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

	public int getIndex() {
		return index;
	}

	public byte getByte(int byteIndex) {
		if (byteIndex < 0 || byteIndex >= MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given byte number is out of range! (" + byteIndex + ")");
		}

		return data[byteIndex];
	}

	public byte[] getByteRange(int startingIndex, int number) {
		if (startingIndex < 0 || startingIndex + number >= MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given byte number is out of range! (" + startingIndex + "-" + (startingIndex + number) + ")");
		}

		return DataUtil.getByteRange(this.data, startingIndex, number);
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
		return bytesToHex(data, (a, b) -> a + ", " + b);
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

	public void updateAccessMode(SectorTrailerBlock sectorTrailerBlock) {
		this.accessMode = CardUtil.getBlockAccessMode(this, sectorTrailerBlock.getAccessBytes());
	}

	@Override
	public String toString() {
		return "\tBlock (" + index + ") " + blockTypeToString(blockType) +
				"\t" + getDataAsHex() +
				"\t" + getAccessMode().toString();
	}
}
