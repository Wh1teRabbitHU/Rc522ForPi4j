package hu.whiterabbit.rc522forpi4j.rc522;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;
import hu.whiterabbit.rc522forpi4j.model.CommunicationStatus;
import hu.whiterabbit.rc522forpi4j.model.ReadResult;
import hu.whiterabbit.rc522forpi4j.model.WriteResult;
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

		Gpio.pinMode(resetPin, Gpio.OUTPUT);
		Gpio.digitalWrite(resetPin, Gpio.HIGH);
		reset();

		writeRC522(T_MODE_REG, (byte) 0x8D);
		writeRC522(T_PRESCALER_REG, (byte) 0x3E);
		writeRC522(T_RELOAD_REG_L, (byte) 30);
		writeRC522(T_RELOAD_REG_H, (byte) 0);
		writeRC522(TX_AUTO_REG, (byte) 0x40);
		writeRC522(MODE_REG, (byte) 0x3D);

		antennaOn();
	}

	private void reset() {
		writeRC522(COMMAND_REG, PCD_RESETPHASE);
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
		byte value = readRC522(TX_CONTROL_REG);

		setBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private void antennaOff() {
		clearBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private WriteResult writeCard(byte command, byte[] data) {
		WriteResult writeResult = new WriteResult();
		byte irq, irq_wait, lastBits;
		int n, i;

		writeResult.setBackData(new byte[16]);
		writeResult.setBackLen(0);

		int dataLen = data.length;

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
			writeRC522(FIFO_DATA_REG, data[i]);

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
			writeResult.setStatus(CommunicationStatus.ERROR);

			return writeResult;
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
					writeResult.setBackBits((n - 1) * 8 + lastBits);
				} else {
					writeResult.setBackBits(n * 8);
				}

				if (n == 0) {
					n = 1;
				} else if (n > MAX_LEN) {
					n = MAX_LEN;
				}

				writeResult.setBackLen(n);

				for (i = 0; i < n; i++) {
					writeResult.getBackData()[i] = readRC522(FIFO_DATA_REG);
				}
			}

			writeResult.setStatus(getStatus(status));

			return writeResult;
		}

		writeResult.setStatus(CommunicationStatus.ERROR);

		return writeResult;
	}

	public WriteResult request(byte reqMode) {
		byte[] tagType = new byte[1];

		writeRC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = reqMode;

		WriteResult writeResult = writeCard(PCD_TRANSCEIVE, tagType);

		if (!writeResult.isSuccess() || writeResult.getBackBits() != 0x10) {
			writeResult.setStatus(CommunicationStatus.ERROR);
		}

		return writeResult;
	}

	//Anti-collision detection.
	//Returns tuple of (error state, tag ID).
	public WriteResult antiColl() {
		byte[] serialNumber = new byte[2];
		int serialNumberCheck = 0;

		writeRC522(BIT_FRAMING_REG, (byte) 0x00);
		serialNumber[0] = PICC_ANTICOLL;
		serialNumber[1] = 0x20;

		WriteResult writeResult = writeCard(PCD_TRANSCEIVE, serialNumber);

		if (writeResult.isSuccess()) {
			if (writeResult.getBackLen() == 5) {
				for (int i = 0; i < 4; i++) {
					serialNumberCheck ^= writeResult.getBackData()[i];
				}

				if (serialNumberCheck != writeResult.getBackData()[4]) {
					logger.error("check error");

					writeResult.setStatus(CommunicationStatus.ERROR);
				}
			} else {
				logger.error("backLen=" + writeResult.getBackLen());
			}
		}

		return writeResult;
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

	public int selectTag(byte[] uid) {
		byte[] data = new byte[9];

		data[0] = PICC_SElECTTAG;
		data[1] = 0x70;

		for (int i = 0, j = 2; i < 5; i++, j++) {
			data[j] = uid[i];
		}

		calculateCRC(data);

		WriteResult writeResult = writeCard(PCD_TRANSCEIVE, data);
		if (writeResult.isSuccess() && writeResult.getBackBits() == 0x18) {
			return writeResult.getBackData()[0];
		} else {
			return 0;
		}
	}

	//Authenticates to use specified block address. Tag must be selected using select_tag(uid) before auth.
	//auth_mode-RFID.auth_a or RFID.auth_b
	//block_address- used to authenticate
	//key-list or tuple with six bytes key
	//uid-list or tuple with four bytes tag ID
	public CommunicationStatus authCard(byte authMode, byte blockAddress, byte[] key, byte[] uid) {
		byte[] data = new byte[12];

		data[0] = authMode;
		data[1] = blockAddress;
		for (int i = 0, j = 2; i < 6; i++, j++) {
			data[j] = key[i];
		}
		for (int i = 0, j = 8; i < 4; i++, j++) {
			data[j] = uid[i];
		}

		WriteResult writeResult = writeCard(PCD_AUTHENT, data);
		if ((readRC522(STATUS_2_REG) & 0x08) == 0) {
			return CommunicationStatus.ERROR;
		}

		return writeResult.getStatus();
	}

	public CommunicationStatus authCard(byte authMode, byte sector, byte block, byte[] key, byte[] uid) {
		return authCard(authMode, sector2BlockAddress(sector, block), key, uid);
	}

	//Ends operations with Crypto1 usage.
	public void stopCrypto() {
		clearBitMask(STATUS_2_REG, (byte) 0x08);
	}

	public WriteResult read(byte sector, byte block) {
		return read(sector2BlockAddress(sector, block));
	}

	//Reads data from block. You should be authenticated before calling read.
	//Returns tuple of (result state, read data).
	//block_address
	//back_data-data to be read,16 bytes
	public WriteResult read(byte blockAddress) {
		byte[] data = new byte[4];

		data[0] = PICC_READ;
		data[1] = blockAddress;

		calculateCRC(data);

		WriteResult writeResult = writeCard(PCD_TRANSCEIVE, data);

		if (writeResult.getBackLen() == 16) {
			writeResult.setStatus(CommunicationStatus.SUCCESS);

			return writeResult;
		}

		return writeResult;
	}

	//Writes data to block. You should be authenticated before calling write.
	//Returns error state.
	//data-16 bytes
	public WriteResult write(byte blockAddress, byte[] data) {
		byte[] buff = new byte[4];
		byte[] buffWrite = new byte[data.length + 2];

		buff[0] = PICC_WRITE;
		buff[1] = blockAddress;

		calculateCRC(buff);

		WriteResult writeResult = writeCard(PCD_TRANSCEIVE, buff);
		if (!writeResult.isSuccess() || writeResult.getBackBits() != 4 || (writeResult.getBackData()[0] & 0x0F) != 0x0A) {
			writeResult.setStatus(CommunicationStatus.ERROR);

			return writeResult;
		}

		System.arraycopy(data, 0, buffWrite, 0, data.length);

		calculateCRC(buffWrite);

		writeResult = writeCard(PCD_TRANSCEIVE, buffWrite);
		if (!writeResult.isSuccess() || writeResult.getBackBits() != 4 || (writeResult.getBackData()[0] & 0x0F) != 0x0A) {
			writeResult.setStatus(CommunicationStatus.ERROR);

			return writeResult;
		}

		return writeResult;
	}

	public WriteResult write(byte sector, byte block, byte[] data) {
		return write(sector2BlockAddress(sector, block), data);
	}

	public byte[] dumpClassic1K(byte[] key, byte[] uid) {
		byte[] data = new byte[1024];

		for (int i = 0; i < 64; i++) {
			CommunicationStatus status = authCard(PICC_AUTHENT1A, (byte) i, key, uid);
			if (status == CommunicationStatus.SUCCESS) {
				WriteResult result = read((byte) i);
				if (result.isSuccess()) {
					System.arraycopy(result.getBackData(), 0, data, i * 64, 16);
				}
			}
		}

		return data;
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
		WriteResult writeResult = request(PICC_REQIDL);
		if (!writeResult.isSuccess()) {
			return writeResult.getStatus();
		}

		writeResult = antiColl();
		if (!writeResult.isSuccess()) {
			return writeResult.getStatus();
		}

		selectTag(writeResult.getBackData());
		System.arraycopy(writeResult.getBackData(), 0, uid, 0, 5);

		return writeResult.getStatus();
	}

	public ReadResult tryToReadCardTag() {
		int[] backBits = new int[1];
		byte[] tagid = new byte[5];

		WriteResult writeResult = request(PICC_REQIDL);
		if (!writeResult.isSuccess()) {
			return new ReadResult(writeResult.getStatus());
		}

		WriteResult status = antiColl();
		if (!status.isSuccess()) {
			return new ReadResult(status.getStatus());
		}

		selectTag(status.getBackData());

		return new ReadResult(status.getStatus());
	}

}
