package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.model.card.Card;
import hu.whiterabbit.rc522forpi4j.rc522.RC522Client;
import hu.whiterabbit.rc522forpi4j.rc522.RC522ClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadCardData {

	private static final Logger logger = LoggerFactory.getLogger(ReadCardData.class);

	public static void main(String[] args) {
		final RC522Client rc522Client = RC522ClientImpl.createInstance();

		logger.info("Starting to read card data");

		try {
			while (true) {
				Card card = rc522Client.readCardData();

				if (card != null) {
					logger.info("Card data: \n{}", card);

					Thread.sleep(2000);
				}

				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
