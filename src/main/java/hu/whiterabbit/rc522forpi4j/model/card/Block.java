package hu.whiterabbit.rc522forpi4j.model.card;

public interface Block {

	int MAX_BLOCK_SIZE = 16;

	int getIndex();

	byte[] getData();

	BlockType getBlockType();

	BlockAccessMode getAccessMode();

	void updateAccessMode(SectorTrailerBlock sectorTrailerBlock);

}
