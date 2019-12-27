package hu.whiterabbit.rc522forpi4j.raspberry;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaspberryPiAdapterImpl implements RaspberryPiAdapter {

	private static final Logger logger = LoggerFactory.getLogger(RaspberryPiAdapterImpl.class);

	private int spiChannel;

	/**
	 * Initializing the RaspberryPiAdapterImpl. It will create a communication channel between the raspberry and
	 * the RC522 unit via SPI.
	 *
	 * @param spiChannel The channel number used for SPI communication
	 * @param speed      Speed of the communication
	 * @param resetPin   The connected reset pin's number
	 * @return The initialization's result.
	 */
	@Override
	public boolean init(int spiChannel, int speed, int resetPin) {
		Gpio.wiringPiSetup();

		this.spiChannel = spiChannel;

		int responseCode = Spi.wiringPiSPISetup(spiChannel, speed);
		if (responseCode > -1) {
			logger.info(" --> Successfully loaded SPI communication");
		} else {
			logger.error(" --> Failed to set up  SPI communication");

			return false;
		}

		Gpio.pinMode(resetPin, Gpio.OUTPUT);
		Gpio.digitalWrite(resetPin, Gpio.HIGH);

		return true;
	}

	/**
	 * Sending data via SPI to the RC522. The returned number is the response code of the communication.
	 *
	 * @param data The sending data
	 * @return The code of the communication's result
	 */
	@Override
	public int wiringPiSPIDataRW(byte[] data) {
		return Spi.wiringPiSPIDataRW(this.spiChannel, data);
	}

}
