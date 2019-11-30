package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.CardAuthKey;
import hu.whiterabbit.rc522forpi4j.model.auth.SectorAuthKey;
import hu.whiterabbit.rc522forpi4j.model.card.*;
import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationResult;
import hu.whiterabbit.rc522forpi4j.util.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey.getFactoryDefaultKey;
import static hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey.getFactoryDefaultSectorKey;
import static hu.whiterabbit.rc522forpi4j.model.card.Card.TAG_ID_SIZE;
import static hu.whiterabbit.rc522forpi4j.model.card.ManufacturerBlock.MANUFACTURER_BLOCK_INDEX;
import static hu.whiterabbit.rc522forpi4j.model.card.ManufacturerBlock.MANUFACTURER_SECTOR_INDEX;
import static hu.whiterabbit.rc522forpi4j.model.card.SectorTrailerBlock.SECTOR_TRAILER_BLOCK_INDEX;
import static hu.whiterabbit.rc522forpi4j.rc522.RC522CommandTable.PICC_AUTHENT1A;
import static hu.whiterabbit.rc522forpi4j.rc522.RC522CommandTable.PICC_AUTHENT1B;
import static hu.whiterabbit.rc522forpi4j.util.CardUtil.getFullAddress;

public class RC522ClientImpl implements RC522Client {

	private static final Logger logger = LoggerFactory.getLogger(RC522ClientImpl.class);

	private static final int RESET_PIN = 22;

	private static final int SPEED = 500000;

	private static final int SPI_CHANNEL = 0;

	private static final RC522Adapter rc522 = new RC522AdapterImpl(SPEED, RESET_PIN, SPI_CHANNEL);

	/**
	 * Select one of your card and read its tagId. If the selection has error or no rad is present then it will return
	 * with a null.
	 *
	 * @return The selected card id or null if no card present or the selection process is failed
	 */
	@Override
	public byte[] readCardTag() {
		CommunicationResult readResult = rc522.selectCard();

		if (!readResult.isSuccess()) {
			return null;
		}

		return readResult.getData(TAG_ID_SIZE);
	}

	/**
	 * Reads all data from your card. If no card present or the process is failed it returns with null. This method is
	 * using the factory default authentication keys.
	 *
	 * @return The Card object with all the readable card data
	 */
	@Override
	public Card readCardData() {
		return readCardData(CardAuthKey.getFactoryDefaultKey());
	}

	/**
	 * Reads all data from your card. If no card present or the process is failed it returns with null. This method is
	 * using the given authentication keys.
	 *
	 * @param cardAuthKey Authentication keys for the whole card
	 * @return The Card object with all the readable card data
	 */
	@Override
	public Card readCardData(CardAuthKey cardAuthKey) {
		byte[] tagId = readCardTag();

		if (tagId == null) {
			return null;
		}

		Card card = new Card(tagId);

		logger.info("Card Read UID: (HEX) {}", card.getTagIdAsString());

		for (int sectorIndex = 0; sectorIndex < Card.SECTOR_COUNT; sectorIndex++) {
			for (int blockIndex = 0; blockIndex < Sector.BLOCK_COUNT; blockIndex++) {
				byte[] data = authAndReadData(sectorIndex, blockIndex, tagId, cardAuthKey.getBlockAuthKey(sectorIndex, blockIndex));

				card.addBlock(sectorIndex, blockIndex, data);
			}
		}

		card.recalculateAccessModes();

		rc522.reset();

		return card;
	}

	/**
	 * Reads a specific sector data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the factory default authentication keys.
	 *
	 * @param sectorIndex The target sector's index
	 * @return The Sector object with all the readable card data
	 */
	@Override
	public Sector readSectorData(int sectorIndex) {
		return readSectorData(sectorIndex, SectorAuthKey.getFactoryDefaultKey(sectorIndex));
	}

	/**
	 * Reads a specific sector data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the given authentication keys.
	 *
	 * @param sectorIndex   The target sector's index
	 * @param sectorAuthKey Authentication keys for the target sector
	 * @return The Sector object with all the readable card data
	 */
	@Override
	public Sector readSectorData(int sectorIndex, SectorAuthKey sectorAuthKey) {
		byte[] tagId = readCardTag();

		if (tagId == null) {
			return null;
		}

		Sector sector = new Sector(sectorIndex);

		logger.info("Card Read UID: (HEX) {}", DataUtil.bytesToHex(tagId));

		for (int blockIndex = 0; blockIndex < Sector.BLOCK_COUNT; blockIndex++) {
			byte[] data = authAndReadData(sectorIndex, blockIndex, tagId, sectorAuthKey.getBlockAuthKey(blockIndex));

			sector.addBlock(blockIndex, data);
		}

		sector.recalculateAccessModes();

		rc522.reset();

		return sector;
	}

	/**
	 * Reads a specific block data from your selected card. If no card present or the process is failed it returns
	 * with null. This method is using the factory default authentication keys.
	 *
	 * @param sectorIndex The target sector's index
	 * @param blockIndex  The target block's index
	 * @return The Block object with all the readable card data
	 */
	@Override
	public Block readBlockData(int sectorIndex, int blockIndex) {
		return readBlockData(sectorIndex, blockIndex, getFactoryDefaultSectorKey(), getFactoryDefaultKey(blockIndex));
	}

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
	@Override
	public Block readBlockData(int sectorIndex, int blockIndex, BlockAuthKey sectorTrailerAuthKey, BlockAuthKey blockAuthKey) {
		byte[] tagId = readCardTag();

		if (tagId == null) {
			return null;
		}

		logger.info("Card Read UID: (HEX) {}", DataUtil.bytesToHex(tagId));

		Block block;
		SectorTrailerBlock sectorTrailerBlock;
		byte[] data = authAndReadData(sectorIndex, blockIndex, tagId, blockAuthKey);

		if (blockIndex == SECTOR_TRAILER_BLOCK_INDEX) {
			block = new SectorTrailerBlock(data);
			sectorTrailerBlock = new SectorTrailerBlock(data);
		} else if (blockIndex == MANUFACTURER_BLOCK_INDEX && sectorIndex == MANUFACTURER_SECTOR_INDEX) {
			block = new ManufacturerBlock(data);

			byte[] sectorTrailerData = authAndReadData(sectorIndex, SECTOR_TRAILER_BLOCK_INDEX, tagId, blockAuthKey);
			sectorTrailerBlock = new SectorTrailerBlock(sectorTrailerData);
		} else {
			block = new DataBlock(blockIndex, data);

			byte[] sectorTrailerData = authAndReadData(sectorIndex, SECTOR_TRAILER_BLOCK_INDEX, tagId, blockAuthKey);
			sectorTrailerBlock = new SectorTrailerBlock(sectorTrailerData);
		}

		block.updateAccessMode(sectorTrailerBlock);

		rc522.reset();

		return block;
	}

	private byte[] authAndReadData(int sectorIndex, int blockIndex, byte[] tagId, BlockAuthKey blockAuthKey) {
		byte fullAddress = getFullAddress(sectorIndex, blockIndex);
		CommunicationResult result = auth(fullAddress, tagId, blockAuthKey);

		if (result.isSuccess()) {
			return readData(fullAddress);
		}

		return null;
	}

	private CommunicationResult auth(byte blockAddress, byte[] tagId, BlockAuthKey blockAuthKey) {
		logger.debug("Authenticate block: {}", blockAddress);

		byte authCommand;

		switch (blockAuthKey.getKeyType()) {
			case AUTH_A:
				authCommand = PICC_AUTHENT1A;
				break;
			case AUTH_B:
				authCommand = PICC_AUTHENT1B;
				break;
			default:
				throw new RuntimeException("Error! Unknown authentication key type: " + blockAuthKey.getKeyType());
		}

		CommunicationResult result = rc522.authCard(authCommand, blockAddress, blockAuthKey.getKey(), tagId);

		if (result.isSuccess()) {
			logger.debug("Successfully authenticated!");
		} else {
			logger.error("Authentication error");
		}

		return result;
	}

	private byte[] readData(byte blockAddress) {
		CommunicationResult result = rc522.read(blockAddress);

		if (!result.isSuccess()) {
			return null;
		}

		return result.getData();
	}

}
