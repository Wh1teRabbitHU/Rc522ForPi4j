package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.List;

public class CardAuthKey {

	private List<SectorAuthKey> sectorAuthKeys;

	public List<SectorAuthKey> getSectorAuthKeys() {
		return sectorAuthKeys;
	}

	public void setSectorAuthKeys(List<SectorAuthKey> sectorAuthKeys) {
		this.sectorAuthKeys = sectorAuthKeys;
	}
}
