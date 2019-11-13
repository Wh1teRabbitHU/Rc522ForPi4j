package hu.whiterabbit.rc522forpi4j.rc522;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;
import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationResult;
import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.whiterabbit.rc522forpi4j.rc522.RC522CommandTable.*;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.getStatus;

@SuppressWarnings({"unused", "WeakerAccess"})
public class RC522Adapter {

	private static final Logger logger = LoggerFactory.getLogger(RC522Adapter.class);

	private static final int MIN_SPEED = 500000;

	private static final int MAX_SPEED = 32000000;

	private final int resetPin;

	private final int speed;

	private final int spiChannel;

	private static final int MAX_LEN = 16;

	public RC522Adapter(int speed, int resetPin, int spiChannel) {
		this.resetPin = resetPin;
		this.speed = speed;
		this.spiChannel = spiChannel;

		if (speed < MIN_SPEED || speed > MAX_SPEED) {
			logger.error("Speed out of range: {}. It should be between {} and {}", speed, MIN_SPEED, MAX_SPEED);

			return;
		}

		init();
	}

	private void init() {
		Gpio.wiringPiSetup();

		int responseCode = Spi.wiringPiSPISetup(spiChannel, speed);
		if (responseCode > -1) {
			logger.info(" --> Successfully loaded SPI communication");
		} else {
			logger.error(" --> Failed to set up  SPI communication");

			return;
		}

		reset();
	}

	public void reset() {
		Gpio.pinMode(resetPin, Gpio.OUTPUT);
		Gpio.digitalWrite(resetPin, Gpio.HIGH);

		writeRC522(COMMAND_REG, PCD_RESETPHASE);
		writeRC522(T_MODE_REG, (byte) 0x8D);
		writeRC522(T_PRESCALER_REG, (byte) 0x3E);
		writeRC522(T_RELOAD_REG_L, (byte) 30);
		writeRC522(T_RELOAD_REG_H, (byte) 0);
		writeRC522(TX_AUTO_REG, (byte) 0x40);
		writeRC522(MODE_REG, (byte) 0x3D);

		antennaOn();
	}

	private void writeRC522(byte address, byte value) {
		byte[] data = new byte[2];

		data[0] = (byte) ((address << 1) & 0x7E);
		data[1] = value;

		int responseCode = Spi.wiringPiSPIDataRW(spiChannel, data);
		if (responseCode == -1) {
			logger.error("Device SPI write error, address={}, value={}", address, value);
		}
	}

	private byte readRC522(byte address) {
		byte[] data = new byte[2];

		data[0] = (byte) (((address << 1) & 0x7E) | 0x80);

		int responseCode = Spi.wiringPiSPIDataRW(spiChannel, data);
		if (responseCode == -1) {
			logger.error("Device SPI read error, address={}", address);
		}

		return data[1];
	}

	private void setBitMask(byte address, byte mask) {
		byte value = readRC522(address);

		writeRC522(address, (byte) (value | mask));
	}

	private void clearBitMask(byte address, byte mask) {
		byte value = readRC522(address);

		writeRC522(address, (byte) (value & (~mask)));
	}

	private void antennaOn() {
		readRC522(TX_CONTROL_REG);

		setBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private void antennaOff() {
		clearBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private CommunicationResult writeCard(byte command, byte[] sendingData) {
		CommunicationResult result = new CommunicationResult();
		byte irq, irq_wait, lastBits;
		int n, i;

		result.setLength(0);

		int dataLen = sendingData.length;

		if (command == PCD_AUTHENT) {
			irq = 0x12;
			irq_wait = 0x10;
		} else if (command == PCD_TRANSCEIVE) {
			irq = 0x77;
			irq_wait = 0x30;
		} else {
			irq = 0;
			irq_wait = 0;
		}

		writeRC522(COMM_I_EN_REG, (byte) (irq | 0x80));
		clearBitMask(COMM_IRQ_REG, (byte) 0x80);
		setBitMask(FIFO_LEVEL_REG, (byte) 0x80);

		writeRC522(COMMAND_REG, PCD_IDLE);

		for (i = 0; i < dataLen; i++)
			writeRC522(FIFO_DATA_REG, sendingData[i]);

		writeRC522(COMMAND_REG, command);
		if (command == PCD_TRANSCEIVE)
			setBitMask(BIT_FRAMING_REG, (byte) 0x80);

		i = 2000;
		do {
			n = readRC522(COMM_IRQ_REG);
			i--;
		} while ((i != 0) && (n & 0x01) <= 0 && (n & irq_wait) <= 0);

		clearBitMask(BIT_FRAMING_REG, (byte) 0x80);

		if (i == 0) {
			result.setStatus(CommunicationStatus.ERROR);

			return result;
		}

		if ((readRC522(ERROR_REG) & 0x1B) == 0x00) {
			int status = MI_OK;

			if ((n & irq & 0x01) > 0) {
				status = MI_NOTAGERR;
			}

			if (command == PCD_TRANSCEIVE) {
				n = readRC522(FIFO_LEVEL_REG);
				lastBits = (byte) (readRC522(CONTROL_REG) & 0x07);

				if (lastBits != 0) {
					result.setBits((n - 1) * 8 + lastBits);
				} else {
					result.setBits(n * 8);
				}

				if (n == 0) {
					n = 1;
				} else if (n > MAX_LEN) {
					n = MAX_LEN;
				}

				result.setLength(n);

				for (i = 0; i < n; i++) {
					result.setDataByte(i, readRC522(FIFO_DATA_REG));
				}
			}

			result.setStatus(getStatus(status));

			return result;
		}

		result.setStatus(CommunicationStatus.ERROR);

		return result;
	}

	public CommunicationResult request(byte reqMode) {
		byte[] tagType = new byte[1];

		writeRC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = reqMode;

		CommunicationResult result = writeCard(PCD_TRANSCEIVE, tagType);

		if (!result.isSuccess() || result.getBits() != 0x10) {
			result.setStatus(CommunicationStatus.ERROR);
		}

		return result;
	}

	//Anti-collision detection.
	//Returns tuple of (error state, tag ID).
	public CommunicationResult antiColl() {
		byte[] serialNumber = new byte[2];
		int serialNumberCheck = 0;

		writeRC522(BIT_FRAMING_REG, (byte) 0x00);
		serialNumber[0] = PICC_ANTICOLL;
		serialNumber[1] = 0x20;

		CommunicationResult result = writeCard(PCD_TRANSCEIVE, serialNumber);

		if (result.isSuccess()) {
			if (result.getLength() == 5) {
				for (int i = 0; i < 4; i++) {
					serialNumberCheck ^= result.getDataByte(i);
				}

				if (serialNumberCheck != result.getDataByte(4)) {
					logger.error("check error");

					result.setStatus(CommunicationStatus.ERROR);
				}
			} else {
				logger.error("backLen=" + result.getLength());
			}
		}

		return result;
	}

	private void calculateCRC(byte[] data) {
		clearBitMask(DIV_IRQ_REG, (byte) 0x04);
		setBitMask(FIFO_LEVEL_REG, (byte) 0x80);

		for (int i = 0; i < data.length - 2; i++) {
			writeRC522(FIFO_DATA_REG, data[i]);
		}

		writeRC522(COMMAND_REG, PCD_CALCCRC);

		int n;
		int i = 255;

		do {
			n = readRC522(DIV_IRQ_REG);
			i--;
		} while ((i != 0) && ((n & 0x04) <= 0));

		data[data.length - 2] = readRC522(CRC_RESULT_REG_L);
		data[data.length - 1] = readRC522(CRC_RESULT_REG_M);
	}

	public boolean selectTag(byte[] uid) {
		byte[] data = new byte[9];

		data[0] = PICC_SElECTTAG;
		data[1] = 0x70;

		for (int i = 0, j = 2; i < 5; i++, j++) {
			data[j] = uid[i];
		}

		calculateCRC(data);

		CommunicationResult result = writeCard(PCD_TRANSCEIVE, data);

		return result.isSuccess() && result.getBits() == 0x18;
	}

	//Authenticates to use specified block address. Tag must be selected using select_tag(uid) before auth.
	//auth_mode-RFID.auth_a or RFID.auth_b
	//block_address- used to authenticate
	//key-list or tuple with six bytes key
	//uid-list or tuple with four bytes tag ID
	public CommunicationResult authCard(byte authMode, byte blockAddress, byte[] key, byte[] uid) {
		byte[] data = new byte[12];

		data[0] = authMode;
		data[1] = blockAddress;
		for (int i = 0, j = 2; i < 6; i++, j++) {
			data[j] = key[i];
		}
		for (int i = 0, j = 8; i < 4; i++, j++) {
			data[j] = uid[i];
		}

		CommunicationResult result = writeCard(PCD_AUTHENT, data);

		if ((readRC522(STATUS_2_REG) & 0x08) == 0) {
			result.setStatus(CommunicationStatus.ERROR);
		}

		return result;
	}

	//Ends operations with Crypto1 usage.
	public void stopCrypto() {
		clearBitMask(STATUS_2_REG, (byte) 0x08);
	}

	//Reads data from block. You should be authenticated before calling read.
	//Returns tuple of (result state, read data).
	//block_address
	//back_data-data to be read,16 bytes
	public CommunicationResult read(byte blockAddress) {
		byte[] data = new byte[4];

		data[0] = PICC_READ;
		data[1] = blockAddress;

		calculateCRC(data);

		CommunicationResult result = writeCard(PCD_TRANSCEIVE, data);

		if (result.getLength() == 16) {
			result.setStatus(CommunicationStatus.SUCCESS);

			return result;
		}

		return result;
	}

	//Writes data to block. You should be authenticated before calling write.
	//Returns error state.
	//data-16 bytes
	public CommunicationResult write(byte blockAddress, byte[] data) {
		byte[] buff = new byte[4];
		byte[] buffWrite = new byte[data.length + 2];

		buff[0] = PICC_WRITE;
		buff[1] = blockAddress;

		calculateCRC(buff);

		CommunicationResult result = writeCard(PCD_TRANSCEIVE, buff);
		if (!result.isSuccess() || result.getBits() != 4 || (result.getDataByte(0) & 0x0F) != 0x0A) {
			result.setStatus(CommunicationStatus.ERROR);

			return result;
		}

		System.arraycopy(data, 0, buffWrite, 0, data.length);

		calculateCRC(buffWrite);

		result = writeCard(PCD_TRANSCEIVE, buffWrite);
		if (!result.isSuccess() || result.getBits() != 4 || (result.getDataByte(0) & 0x0F) != 0x0A) {
			result.setStatus(CommunicationStatus.ERROR);

			return result;
		}

		return result;
	}

	//Convert sector  to blockaddress
	//sector-0~15
	//block-0~3
	//return blockaddress
	private byte sector2BlockAddress(byte sector, byte block) {
		if (sector < 0 || sector > 15 || block < 0 || block > 3) {
			return (byte) (-1);
		}

		return (byte) (sector * 4 + block);
	}

	//uid-5 bytes
	public CommunicationStatus selectMirareOne(byte[] uid) {
		CommunicationResult result = request(PICC_REQIDL);
		if (!result.isSuccess()) {
			return result.getStatus();
		}

		result = antiColl();
		if (!result.isSuccess()) {
			return result.getStatus();
		}

		selectTag(result.getData());
		System.arraycopy(result.getData(), 0, uid, 0, 5);

		return CommunicationStatus.SUCCESS;
	}

}
