package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.model.auth.AuthKeyType;
import hu.whiterabbit.rc522forpi4j.model.auth.BlockAuthKey;
import hu.whiterabbit.rc522forpi4j.rc522.RC522Client;
import hu.whiterabbit.rc522forpi4j.rc522.RC522ClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class TryToAuthCard {

	private static final Logger logger = LoggerFactory.getLogger(TryToAuthCard.class);

	private static final List<byte[]> availableKeys = Arrays.asList(
			new byte[]{(byte) 0xA0, (byte) 0xB0, (byte) 0xC0, (byte) 0xD0, (byte) 0xE0, (byte) 0xF0},
			new byte[]{(byte) 0xA1, (byte) 0xB1, (byte) 0xC1, (byte) 0xD1, (byte) 0xE1, (byte) 0xF1},
			new byte[]{(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5},
			new byte[]{(byte) 0xB0, (byte) 0xB1, (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, (byte) 0xB5},
			new byte[]{(byte) 0x4D, (byte) 0x3A, (byte) 0x99, (byte) 0xC3, (byte) 0x51, (byte) 0xDD},
			new byte[]{(byte) 0x1A, (byte) 0x98, (byte) 0x2C, (byte) 0x7E, (byte) 0x45, (byte) 0x9A},
			new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00},
			new byte[]{(byte) 0xD3, (byte) 0xF7, (byte) 0xD3, (byte) 0xF7, (byte) 0xD3, (byte) 0xF7},
			new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF},
			new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}
	);

	public static void main(String[] args) throws InterruptedException {
		final RC522Client rc522Client = RC522ClientImpl.createInstance();

		while (true) {
			byte[] tagId = rc522Client.readCardTag();

			// No card is present
			if (tagId == null) {
				Thread.sleep(10);

				continue;
			}

			logger.info("Trying to authenticate blocks on the following card: {}", bytesToHex(tagId));
			logger.info("-------------------------------------------------------------------------");

			for (int sectorIndex = 0; sectorIndex < 16; sectorIndex++) {
				for (int blockIndex = 0; blockIndex < 4; blockIndex++) {
					logger.info("Trying to authenticate {}. sector's {}. block...", sectorIndex, blockIndex);
					logger.info("-------------------------------------------------------------------------");

					BlockAuthKey authKey = null;

					for (byte[] key : availableKeys) {
						BlockAuthKey authKeyA = new BlockAuthKey(blockIndex);
						BlockAuthKey authKeyB = new BlockAuthKey(blockIndex);

						authKeyA.setKeyType(AuthKeyType.AUTH_A);
						authKeyA.setKey(key);

						authKeyB.setKeyType(AuthKeyType.AUTH_B);
						authKeyB.setKey(key);

						boolean aKeyResult = rc522Client.checkAuth(authKeyA, tagId, sectorIndex, blockIndex);

						if (aKeyResult) {
							authKey = authKeyA;

							break;
						} else {
							logger.info("Fail: {}", authKeyA);
						}

						boolean bKeyResult = rc522Client.checkAuth(authKeyB, tagId, sectorIndex, blockIndex);

						if (bKeyResult) {
							authKey = authKeyB;

							break;
						} else {
							logger.info("Fail: {}", authKeyB);
						}
					}

					if (authKey == null) {
						logger.info("Couldn't authenticate this block with the given key list!");
					} else {
						logger.info("Success: {}", authKey);
					}
					logger.info("-------------------------------------------------------------------------");
				}
			}

			logger.info("Finished with checking the card's authentications!");

			Thread.sleep(3000);
		}
	}
}
