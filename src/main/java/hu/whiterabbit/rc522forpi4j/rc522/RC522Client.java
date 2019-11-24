package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.card.Block;
import hu.whiterabbit.rc522forpi4j.model.card.Card;
import hu.whiterabbit.rc522forpi4j.model.card.Sector;

public interface RC522Client {

	Card readCardData();

	Sector readSectorData(int sectorIndex);

	Block readBlockData(int sectorIndex, int blockIndex);

}
