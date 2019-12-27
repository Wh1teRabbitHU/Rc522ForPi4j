package hu.whiterabbit.rc522forpi4j.model.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hu.whiterabbit.rc522forpi4j.model.card.Sector.BLOCK_COUNT;

public class SectorAuthKey {

	private final int sectorIndex;

	private final List<BlockAuthKey> blockAuthKeys = new ArrayList<>();

	public SectorAuthKey(int sectorIndex) {
		this.sectorIndex = sectorIndex;
	}

	public int getSectorIndex() {
		return sectorIndex;
	}

	public List<BlockAuthKey> getBlockAuthKeys() {
		return blockAuthKeys;
	}

	public BlockAuthKey getBlockAuthKey(int blockIndex) {
		return blockAuthKeys.stream()
				.filter(blockAuthKey -> Objects.equals(blockAuthKey.getBlockIndex(), blockIndex))
				.findFirst()
				.orElse(null);
	}

	public void addBlockAuthKey(BlockAuthKey blockAuthKey) {
		blockAuthKeys.add(blockAuthKey);
	}

	public static SectorAuthKey getFactoryDefaultKey(int sectorIndex) {
		SectorAuthKey sectorAuthKey = new SectorAuthKey(sectorIndex);

		for (int i = 0; i < BLOCK_COUNT; i++) {
			sectorAuthKey.addBlockAuthKey(BlockAuthKey.getFactoryDefaultKey(i));
		}

		return sectorAuthKey;
	}
}
