package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.ArrayList;
import java.util.List;

public class Sector {

	public static final int MAX_SECTOR_SIZE = 4;

	private final int number;

	private final List<Block> blockList = new ArrayList<>();

	public Sector(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public Block getBlock(int blockNumber) {
		if (blockNumber < 0 || blockNumber >= MAX_SECTOR_SIZE) {
			throw new RuntimeException("Given block number is out of range! (" + blockNumber + ")");
		}

		return blockList
				.stream()
				.filter(block -> block.getNumber() == blockNumber)
				.findFirst()
				.orElse(null);
	}

	public List<Block> getBlockList() {
		return blockList;
	}

	public Block getSectorTrailerBlock() {
		return blockList.stream()
				.filter(block -> block.getNumber() == Block.SECTOR_TRAILER_BLOCK_INDEX)
				.findFirst()
				.orElse(null);
	}

	public Block getManufacturerBlock() {
		if (number != Block.MANUFACTURER_SECTOR_INDEX) {
			return null;
		}

		return blockList.stream()
				.filter(block -> block.getNumber() == Block.MANUFACTURER_BLOCK_INDEX)
				.findFirst()
				.orElse(null);
	}

	public void addBlock(Block block) {
		Block existingBlock = getBlock(block.getNumber());

		if (existingBlock != null) {
			throw new RuntimeException("Cannot add the given block to this sector. " +
					"Block is already added with this number: " + block.getNumber());
		}

		blockList.add(block);
	}

	@Override
	public String toString() {
		return "Sector (" + number +
				")\n" + blockList
				.stream()
				.map(Block::toString)
				.reduce((a, b) -> a + "\n" + b)
				.orElse("");
	}
}
