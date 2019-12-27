package hu.whiterabbit.rc522forpi4j.raspberry;

public interface RaspberryPiAdapter {

	/**
	 * Initializing the RaspberryPiAdapter. It will create a communication channel between the raspberry and
	 * the RC522 unit via SPI.
	 *
	 * @param spiChannel The channel number used for SPI communication
	 * @param speed      Speed of the communication
	 * @param resetPin   The connected reset pin's number
	 * @return The initialization's result.
	 */
	boolean init(int spiChannel, int speed, int resetPin);

	/**
	 * Sending data via SPI to the RC522. The returned number is the response code of the communication.
	 *
	 * @param data The sending data
	 * @return The code of the communication's result
	 */
	int wiringPiSPIDataRW(byte[] data);

}
