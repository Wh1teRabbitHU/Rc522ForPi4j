package hu.whiterabbit.rc522forpi4j.model.card;

public interface Block {

	int BYTE_COUNT = 16;

	int getIndex();

	byte[] getData();

	BlockType getBlockType();

	BlockAccessMode getAccessMode();

	BlockReadStatus getReadStatus();

	void updateAccessMode(SectorTrailerBlock sectorTrailerBlock);

}
