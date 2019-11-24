package hu.whiterabbit.rc522forpi4j.model.card;

import java.nio.charset.StandardCharsets;

import static hu.whiterabbit.rc522forpi4j.model.card.Sector.MAX_SECTOR_SIZE;
import static hu.whiterabbit.rc522forpi4j.util.CardUtil.blockTypeToString;
import static hu.whiterabbit.rc522forpi4j.util.CardUtil.getBlockAccessMode;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class DataBlock implements Block {

	private final int index;

	private final byte[] data;

	private BlockType blockType;

	private BlockAccessMode accessMode;

	public DataBlock(int index, byte[] data) {
		if (index < 0 || index >= MAX_SECTOR_SIZE) {
			throw new RuntimeException("Given block index is out of range! (" + index + ")");
		} else if (data != null && data.length > MAX_BLOCK_SIZE) {
			throw new RuntimeException("Given data array is too large! (" + data.length +
					") It should be less than " + MAX_BLOCK_SIZE);
		}

		this.index = index;

		if (data == null) {
			this.data = new byte[MAX_BLOCK_SIZE];
		} else {
			this.data = data;
		}

		blockType = BlockType.DATA;
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
		this.accessMode = getBlockAccessMode(this, sectorTrailerBlock.getAccessBytes());
	}

	@Override
	public String toString() {
		return "\tBlock (" + getIndex() + ") " + blockTypeToString(getBlockType()) +
				"\t" + getDataAsHex() +
				"\t\t" + getAccessMode().toString();
	}
}
