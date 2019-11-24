package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.ArrayList;
import java.util.List;

public class Sector {

	public static final int MAX_SECTOR_SIZE = 4;

	private final int index;

	private SectorTrailerBlock sectorTrailerBlock;

	private ManufacturerBlock manufacturerBlock;

	private final List<DataBlock> dataBlockList = new ArrayList<>();

	public Sector(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public DataBlock getBlock(int blockNumber) {
		if (blockNumber < 0 || blockNumber >= MAX_SECTOR_SIZE) {
			throw new RuntimeException("Given block number is out of range! (" + blockNumber + ")");
		}

		return dataBlockList
				.stream()
				.filter(dataBlock -> dataBlock.getIndex() == blockNumber)
				.findFirst()
				.orElse(null);
	}

	public List<DataBlock> getDataBlockList() {
		return dataBlockList;
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

		dataBlockList.add(dataBlock);
	}


	@Override
	public String toString() {
		String sectorHeaderString = "Sector (" + index + ")\n";
		String manufacturerBlockString = this.manufacturerBlock == null ? "" : this.manufacturerBlock.toString() + "\n";
		String dataBlockString = this.dataBlockList
				.stream()
				.map(DataBlock::toString)
				.reduce((a, b) -> a + "\n" + b)
				.orElse("") + "\n";
		String sectorTrailerBlockString = this.sectorTrailerBlock.toString();

		return sectorHeaderString + manufacturerBlockString + dataBlockString + sectorTrailerBlockString;
	}
}
