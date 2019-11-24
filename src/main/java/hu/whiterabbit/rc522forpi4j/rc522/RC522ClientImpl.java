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
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return null;
		}

		Card card = new Card(tagId);

		logger.info("Card Read UID: (HEX) {}", card.getTagIdAsString());

		for (int sectorIndex = 0; sectorIndex < Card.MAX_CARD_SIZE; sectorIndex++) {
			for (int blockIndex = 0; blockIndex < Sector.MAX_SECTOR_SIZE; blockIndex++) {
				byte[] data = authAndReadData(sectorIndex, blockIndex, tagId);

				card.addBlock(sectorIndex, blockIndex, data);
			}
		}

		card.recalculateAccessModes();

		rc522.reset();

		return card;
	}

	@Override
	public Sector readSectorData(int sectorIndex) {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return null;
		}

		Sector sector = new Sector(sectorIndex);

		logger.info("Card Read UID: (HEX) {}", DataUtil.bytesToHex(tagId));

		for (int blockIndex = 0; blockIndex < Sector.MAX_SECTOR_SIZE; blockIndex++) {
			byte[] data = authAndReadData(sectorIndex, blockIndex, tagId);

			sector.addBlock(blockIndex, data);
		}

		sector.recalculateAccessModes();

		rc522.reset();

		return sector;
	}

	@Override
	public Block readBlockData(int sectorIndex, int blockIndex) {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return null;
		}

		logger.info("Card Read UID: (HEX) {}", DataUtil.bytesToHex(tagId));

		Block block;
		byte[] data = authAndReadData(sectorIndex, blockIndex, tagId);

		if (blockIndex == SECTOR_TRAILER_BLOCK_INDEX) {
			block = new SectorTrailerBlock(data);
		} else if (blockIndex == MANUFACTURER_BLOCK_INDEX && sectorIndex == MANUFACTURER_SECTOR_INDEX) {
			block = new ManufacturerBlock(data);
		} else {
			block = new DataBlock(blockIndex, data);
		}

		rc522.reset();

		return block;
	}

	private byte[] authAndReadData(int sectorIndex, int blockIndex, byte[] tagId) {
		byte fullAddress = getFullAddress(sectorIndex, blockIndex);
		CommunicationResult result = auth(fullAddress, tagId);

		if (result.isSuccess()) {
			return readData(fullAddress);
		}

		return null;
	}

	private CommunicationResult auth(byte blockAddress, byte[] tagId) {
		logger.debug("Authenticate block: {}", blockAddress);

		CommunicationResult result = rc522.authCard(PICC_AUTHENT1A, blockAddress, KEY_B, tagId);

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
