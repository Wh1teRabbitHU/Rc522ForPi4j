package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.card.*;
import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationResult;
import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationStatus;
import hu.whiterabbit.rc522forpi4j.util.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final byte[] KEY_A = new byte[]{(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03};
	private static final byte[] KEY_B = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

	private static final RC522Adapter rc522 = new RC522Adapter(SPEED, RESET_PIN, SPI_CHANNEL);

	@Override
	public Card readCardData() {
		return readCardData(CardAuthKey.getFactoryDefaultKey());
	}

	@Override
	public Card readCardData(CardAuthKey cardAuthKey) {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return null;
		}

		Card card = new Card(tagId);

		logger.info("Card Read UID: (HEX) {}", card.getTagIdAsString());

		for (int sectorIndex = 0; sectorIndex < Card.MAX_CARD_SIZE; sectorIndex++) {
			for (int blockIndex = 0; blockIndex < Sector.MAX_SECTOR_SIZE; blockIndex++) {
				byte[] data = authAndReadData(sectorIndex, blockIndex, tagId, cardAuthKey.getBlockAuthKey(sectorIndex, blockIndex));

				card.addBlock(sectorIndex, blockIndex, data);
			}
		}

		card.recalculateAccessModes();

		rc522.reset();

		return card;
	}

	@Override
	public Sector readSectorData(int sectorIndex) {
		return readSectorData(sectorIndex, SectorAuthKey.getFactoryDefaultKey(sectorIndex));
	}

	@Override
	public Sector readSectorData(int sectorIndex, SectorAuthKey sectorAuthKey) {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return null;
		}

		Sector sector = new Sector(sectorIndex);

		logger.info("Card Read UID: (HEX) {}", DataUtil.bytesToHex(tagId));

		for (int blockIndex = 0; blockIndex < Sector.MAX_SECTOR_SIZE; blockIndex++) {
			byte[] data = authAndReadData(sectorIndex, blockIndex, tagId, sectorAuthKey.getBlockAuthKey(blockIndex));

			sector.addBlock(blockIndex, data);
		}

		sector.recalculateAccessModes();

		rc522.reset();

		return sector;
	}

	@Override
	public Block readBlockData(int sectorIndex, int blockIndex) {
		return readBlockData(sectorIndex, blockIndex, BlockAuthKey.getFactoryDefaultKey(blockIndex));
	}

	@Override
	public Block readBlockData(int sectorIndex, int blockIndex, BlockAuthKey blockAuthKey) {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
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
