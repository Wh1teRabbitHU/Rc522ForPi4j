package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.model.CardData;
import hu.whiterabbit.rc522forpi4j.rc522.RC522Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadData {

	private static final Logger logger = LoggerFactory.getLogger(ReadData.class);

	public static void main(String[] args) {
		final RC522Client rc522Client = new RC522Client();

		logger.info("Starting to read data");

		try {
			while (true) {
				CardData cardData = rc522Client.readCardData();

				if (cardData != null) {
					logger.info("Card data: {}", cardData);
				}

				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
