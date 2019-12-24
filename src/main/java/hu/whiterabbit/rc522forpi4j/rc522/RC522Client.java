package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.CardAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.SectorAuthKey;
import hu.whiterabbit.rc522forpi4j.model.card.Block;
import hu.whiterabbit.rc522forpi4j.model.card.Card;
import hu.whiterabbit.rc522forpi4j.model.card.Sector;

public interface RC522Client {

	void init();

	byte[] readCardTag();

	Card readCardData();

	Card readCardData(CardAuthKey cardAuthKey);

	Sector readSectorData(int sectorIndex);

	Sector readSectorData(int sectorIndex, SectorAuthKey sectorAuthKey);

	Block readBlockData(int sectorIndex, int blockIndex);

	Block readBlockData(int sectorIndex, int blockIndex, BlockAuthKey sectorTrailerAuthKey, BlockAuthKey blockAuthKey);

}
