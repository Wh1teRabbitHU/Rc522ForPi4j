package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.List;

public class SectorAuthKey {

	private int sectorIndex;

	private List<BlockAuthKey> blockAuthKeys;

	public int getSectorIndex() {
		return sectorIndex;
	}

	public void setSectorIndex(int sectorIndex) {
		this.sectorIndex = sectorIndex;
	}

	public List<BlockAuthKey> getBlockAuthKeys() {
		return blockAuthKeys;
	}

	public void setBlockAuthKeys(List<BlockAuthKey> blockAuthKeys) {
		this.blockAuthKeys = blockAuthKeys;
	}
}
