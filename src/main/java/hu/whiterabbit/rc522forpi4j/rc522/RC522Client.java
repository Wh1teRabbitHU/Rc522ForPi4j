package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.CardAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.SectorAuthKey;
import hu.whiterabbit.rc522forpi4j.model.card.*;

public interface RC522Client {

	Card readCardData();

	Card readCardData(CardAuthKey cardAuthKey);

	Sector readSectorData(int sectorIndex);

	Sector readSectorData(int sectorIndex, SectorAuthKey sectorAuthKey);

	Block readBlockData(int sectorIndex, int blockIndex);

	Block readBlockData(int sectorIndex, int blockIndex, BlockAuthKey blockAuthKey);

}
