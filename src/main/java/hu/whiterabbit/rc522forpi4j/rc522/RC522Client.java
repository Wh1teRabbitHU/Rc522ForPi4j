package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.CardData;
import hu.whiterabbit.rc522forpi4j.model.CommunicationResult;
import hu.whiterabbit.rc522forpi4j.model.CommunicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.whiterabbit.rc522forpi4j.rc522.RC522CommandTable.PICC_AUTHENT1A;

public class RC522Client {

	private static final Logger logger = LoggerFactory.getLogger(RC522Client.class);

	private static final int RESET_PIN = 22;

	private static final int SPEED = 500000;

	private static final int SPI_CHANNEL = 0;

	private static final byte[] KEY_A = new byte[]{(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03};
	private static final byte[] KEY_B = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

	private static final RC522Adapter rc522 = new RC522Adapter(SPEED, RESET_PIN, SPI_CHANNEL);

	public CardData readCardData() {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return null;
		}

		CardData cardData = new CardData(tagId);

		logger.info("Card Read UID: (HEX) {}", cardData.getTagIdAsString());

		for (byte block = 0; block < 64; block++) {
			CommunicationResult result = authenticate(block, tagId);

			if (result.isSuccess()) {
				cardData.addDataBlock(block, readData(block));
			}
		}

		rc522.reset();

		return cardData;
	}

	private CommunicationResult authenticate(byte blockAddress, byte[] tagId) {
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
