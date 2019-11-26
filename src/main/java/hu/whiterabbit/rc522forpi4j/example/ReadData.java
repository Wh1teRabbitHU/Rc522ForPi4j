package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.model.card.Block;
import hu.whiterabbit.rc522forpi4j.model.card.Card;
import hu.whiterabbit.rc522forpi4j.model.card.Sector;
import hu.whiterabbit.rc522forpi4j.rc522.RC522ClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadData {

	private static final Logger logger = LoggerFactory.getLogger(ReadData.class);

	public static void main(String[] args) {
		final RC522ClientImpl rc522Client = new RC522ClientImpl();

		logger.info("Starting to read data");

		try {
			while (true) {
				Card card = rc522Client.readCardData();

				if (card != null) {
					logger.info("Card data: \n{}", card);

					Thread.sleep(100);

					Sector sector = rc522Client.readSectorData(1);

					if (sector != null) {
						logger.info("Sector data: \n{}", sector);

						Thread.sleep(100);

						Block block = rc522Client.readBlockData(1, 0);

						if (block != null) {
							logger.info("Block data: \n{}", block);
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
