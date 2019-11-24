package hu.whiterabbit.rc522forpi4j.model.card;

public abstract class Block {

	static final int MAX_BLOCK_SIZE = 16;

	static final int SECTOR_TRAILER_BLOCK_INDEX = 3;

	static final int MANUFACTURER_BLOCK_INDEX = 0;

	static final int MANUFACTURER_SECTOR_INDEX = 0;

	private final int index;

	private final byte[] data;

	private BlockType blockType;

	private BlockAccessMode accessMode;

	public Block(int index, byte[] data) {
		this.index = index;
		this.data = data;
	}

	public int getIndex() {
		return index;
	}

	public byte[] getData() {
		return data;
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

	public void setAccessMode(BlockAccessMode accessMode) {
		this.accessMode = accessMode;
	}
}
