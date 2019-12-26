package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.model.card.Sector;
import hu.whiterabbit.rc522forpi4j.rc522.RC522Client;
import hu.whiterabbit.rc522forpi4j.rc522.RC522ClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadSectorData {

	private static final Logger logger = LoggerFactory.getLogger(ReadSectorData.class);

	public static void main(String[] args) {
		final RC522Client rc522Client = RC522ClientImpl.createInstance();

		logger.info("Starting to read sector data");

		try {
			while (true) {
				boolean hasCard = rc522Client.hasCard();

				if (hasCard) {
					for (int sectorIndex = 0; sectorIndex < 15; sectorIndex++) {
						Sector sector = rc522Client.readSectorData(sectorIndex);

						if (sector != null) {
							logger.info("Sector data: \n{}", sector);

							Thread.sleep(100);
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
