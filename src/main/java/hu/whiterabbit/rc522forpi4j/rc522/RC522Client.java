package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.CardAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.SectorAuthKey;
import hu.whiterabbit.rc522forpi4j.model.card.Block;
import hu.whiterabbit.rc522forpi4j.model.card.Card;
import hu.whiterabbit.rc522forpi4j.model.card.Sector;

public interface RC522Client {

	/**
	 * Initializing the raspberryPi and RC522 adapters
	 */
	void init();

	/**
	 * This method checks for nearby cards. If no cards detected, it returns false otherwise, if a card is present,
	 * then returns true. Important: This method will returns true when the card is present, but it's not readable!
	 *
	 * @return Has any card near to the reader?
	 */
	boolean hasCard();

	/**
	 * Select one of your card and read its tagId. If the selection has error or no rad is present then it will return
	 * with a null.
	 *
	 * @return The selected card id or null if no card present or the selection process is failed
	 */
	byte[] readCardTag();

	/**
	 * Reads all data from your card. If no card present or the process is failed it returns with null. This method is
	 * using the factory default authentication keys.
	 *
	 * @return The Card object with all the readable card data
	 */
	Card readCardData();

	/**
	 * Reads all data from your card. If no card present or the process is failed it returns with null. This method is
	 * using the given authentication keys.
	 *
	 * @param cardAuthKey Authentication keys for the whole card
	 * @return The Card object with all the readable card data
	 */
	Card readCardData(CardAuthKey cardAuthKey);

	/**
	 * Reads a specific sector data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the factory default authentication keys.
	 *
	 * @param sectorIndex The target sector's index
	 * @return The Sector object with all the readable card data
	 */
	Sector readSectorData(int sectorIndex);

	/**
	 * Reads a specific sector data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the given authentication keys.
	 *
	 * @param sectorIndex   The target sector's index
	 * @param sectorAuthKey Authentication keys for the target sector
	 * @return The Sector object with all the readable card data
	 */
	Sector readSectorData(int sectorIndex, SectorAuthKey sectorAuthKey);

	/**
	 * Reads a specific block data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the factory default authentication keys.
	 *
	 * @param sectorIndex The target sector's index
	 * @param blockIndex  The target block's index
	 * @return The Block object with all the readable card data
	 */
	Block readBlockData(int sectorIndex, int blockIndex);

	/**
	 * Reads a specific block data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the given authentication keys.
	 *
	 * @param sectorIndex          The target sector's index
	 * @param blockIndex           The target block's index
	 * @param sectorTrailerAuthKey Authentication key for the target block's sector trailer block
	 * @param blockAuthKey         Authentication key for the target block
	 * @return The Block object with all the readable card data
	 */
	Block readBlockData(int sectorIndex, int blockIndex, BlockAuthKey sectorTrailerAuthKey, BlockAuthKey blockAuthKey);

}
