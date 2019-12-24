package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.ArrayList;
import java.util.List;

import static hu.whiterabbit.rc522forpi4j.model.card.ManufacturerBlock.MANUFACTURER_BLOCK_INDEX;
import static hu.whiterabbit.rc522forpi4j.model.card.ManufacturerBlock.MANUFACTURER_SECTOR_INDEX;
import static hu.whiterabbit.rc522forpi4j.model.card.SectorTrailerBlock.SECTOR_TRAILER_BLOCK_INDEX;

public class Sector {

	public static final int BLOCK_COUNT = 4;

	private final int index;

	private SectorTrailerBlock sectorTrailerBlock;

	private ManufacturerBlock manufacturerBlock;

	private final List<DataBlock> dataBlocks = new ArrayList<>();

	public Sector(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public DataBlock getBlock(int blockNumber) {
		if (blockNumber < 0 || blockNumber >= BLOCK_COUNT) {
			throw new RuntimeException("Given block number is out of range! (" + blockNumber + ")");
		}

		return dataBlocks
				.stream()
				.filter(dataBlock -> dataBlock.getIndex() == blockNumber)
				.findFirst()
				.orElse(null);
	}

	public List<DataBlock> getDataBlocks() {
		return dataBlocks;
	}

	public SectorTrailerBlock getSectorTrailerBlock() {
		return sectorTrailerBlock;
	}

	public void setSectorTrailerBlock(SectorTrailerBlock sectorTrailerBlock) {
		this.sectorTrailerBlock = sectorTrailerBlock;
	}

	public ManufacturerBlock getManufacturerBlock() {
		return manufacturerBlock;
	}

	public void setManufacturerBlock(ManufacturerBlock manufacturerBlock) {
		this.manufacturerBlock = manufacturerBlock;
	}

	public void addBlock(DataBlock dataBlock) {
		DataBlock existingDataBlock = getBlock(dataBlock.getIndex());

		if (existingDataBlock != null) {
			throw new RuntimeException("Cannot add the given block to this sector. " +
					"Block is already added with this number: " + dataBlock.getIndex());
		}

		dataBlocks.add(dataBlock);
	}

	public void addBlock(int blockIndex, byte[] byteData, BlockReadStatus readStatus) {
		if (blockIndex == SECTOR_TRAILER_BLOCK_INDEX) {
			this.setSectorTrailerBlock(new SectorTrailerBlock(byteData, readStatus));
		} else if (blockIndex == MANUFACTURER_BLOCK_INDEX && this.index == MANUFACTURER_SECTOR_INDEX) {
			this.setManufacturerBlock(new ManufacturerBlock(byteData, readStatus));
		} else {
			this.addBlock(new DataBlock(blockIndex, byteData, readStatus));
		}
	}

	public void recalculateAccessModes() {
		SectorTrailerBlock sectorTrailerBlock = this.getSectorTrailerBlock();

		if (sectorTrailerBlock == null) {
			return;
		}

		sectorTrailerBlock.updateAccessMode(sectorTrailerBlock);

		if (this.getManufacturerBlock() != null) {
			this.getManufacturerBlock().updateAccessMode(sectorTrailerBlock);
		}

		for (int blockIndex = 0; blockIndex < BLOCK_COUNT; blockIndex++) {
			DataBlock dataBlock = this.getBlock(blockIndex);

			if (dataBlock != null) {
				dataBlock.updateAccessMode(sectorTrailerBlock);
			}
		}
	}

	@Override
	public String toString() {
		String sectorHeaderString = "Sector (" + index + ")\n";
		String manufacturerBlockString = this.manufacturerBlock == null ? "" : this.manufacturerBlock.toString() + "\n";
		String dataBlockString = this.dataBlocks
				.stream()
				.map(DataBlock::toString)
				.reduce((a, b) -> a + "\n" + b)
				.orElse("") + "\n";
		String sectorTrailerBlockString = this.sectorTrailerBlock.toString();

		return sectorHeaderString + manufacturerBlockString + dataBlockString + sectorTrailerBlockString;
	}
}
