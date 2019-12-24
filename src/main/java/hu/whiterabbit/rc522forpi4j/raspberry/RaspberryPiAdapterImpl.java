package hu.whiterabbit.rc522forpi4j.raspberry;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaspberryPiAdapterImpl implements RaspberryPiAdapter {

	private static final Logger logger = LoggerFactory.getLogger(RaspberryPiAdapterImpl.class);

	@Override
	public boolean init(int spiChannel, int speed, int resetPin) {
		Gpio.wiringPiSetup();

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

	@Override
	public int wiringPiSPIDataRW(int channel, byte[] data) {
		return Spi.wiringPiSPIDataRW(channel, data);
	}

}
