package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.model.card.Block;
import hu.whiterabbit.rc522forpi4j.rc522.RC522Client;
import hu.whiterabbit.rc522forpi4j.rc522.RC522ClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadBlockData {
	private static final Logger logger = LoggerFactory.getLogger(ReadBlockData.class);

	public static void main(String[] args) {
		final RC522Client rc522Client = RC522ClientImpl.createInstance();

		logger.info("Starting to read data");

		try {
			while (true) {
				boolean hasCard = rc522Client.hasCard();

				if (hasCard) {
					for (int sectorIndex = 0; sectorIndex < 15; sectorIndex++) {
						for (int blockIndex = 0; blockIndex < 4; blockIndex++) {
							Block block = rc522Client.readBlockData(sectorIndex, blockIndex);

							if (block != null) {
								logger.info("Block data: \n{}", block);
							}
						}
					}

					Thread.sleep(2000);
				}

				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
