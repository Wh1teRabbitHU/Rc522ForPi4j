package hu.whiterabbit.rc522forpi4j.example;

import hu.whiterabbit.rc522forpi4j.service.RC522Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadData {

	private static final Logger logger = LoggerFactory.getLogger(ReadData.class);

	public static void main(String[] args) {
		final RC522Reader rc522Reader = new RC522Reader();

		logger.info("Starting to read data");

		try {
			while (true) {
				String tag = rc522Reader.readTag();

				if (tag.equals("")) {
					continue;
				}

				logger.info("Tag: {}", tag);

				rc522Reader.read();

				Thread.sleep(10);
			}
		} catch (InterruptedException ignored) {
		}
	}

}
