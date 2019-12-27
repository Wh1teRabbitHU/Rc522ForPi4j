package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationResult;
import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationStatus;
import hu.whiterabbit.rc522forpi4j.raspberry.RaspberryPiAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.whiterabbit.rc522forpi4j.model.card.Block.BYTE_COUNT;
import static hu.whiterabbit.rc522forpi4j.model.card.Card.TAG_ID_SIZE;
import static hu.whiterabbit.rc522forpi4j.rc522.RC522CommandTable.*;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.getStatus;
import static java.lang.System.arraycopy;

@SuppressWarnings({"WeakerAccess"})
public class RC522AdapterImpl implements RC522Adapter {

	private static final Logger logger = LoggerFactory.getLogger(RC522AdapterImpl.class);

	private static final int MIN_SPEED = 500000;

	private static final int MAX_SPEED = 32000000;

	private static final int MAX_LEN = 16;

	private final RaspberryPiAdapter raspberryPiAdapter;

	public RC522AdapterImpl(RaspberryPiAdapter raspberryPiAdapter) {
		this.raspberryPiAdapter = raspberryPiAdapter;
	}

	/**
	 * Initializing the RC522Adapter. It creates and initialize the raspberryPiAdapter instance and resets the RC522
	 * module to it's default state
	 *
	 * @param speed      Speed of the communication
	 * @param resetPin   The connected reset pin's number
	 * @param spiChannel The channel number used for SPI communication
	 */
	@Override
	public void init(int speed, int resetPin, int spiChannel) {
		if (speed < MIN_SPEED || speed > MAX_SPEED) {
			logger.error("Speed out of range: {}. It should be between {} and {}", speed, MIN_SPEED, MAX_SPEED);

			return;
		}

		raspberryPiAdapter.init(spiChannel, speed, resetPin);

		reset();
	}

	/**
	 * Resets your RC522 module to it's default state
	 */
	@Override
	public void reset() {
		writeRC522(COMMAND_REG, PCD_RESETPHASE);
		writeRC522(T_MODE_REG, (byte) 0x8D);
		writeRC522(T_PRESCALER_REG, (byte) 0x3E);
		writeRC522(T_RELOAD_REG_L, (byte) 30);
		writeRC522(T_RELOAD_REG_H, (byte) 0);
		writeRC522(TX_ASK_REG, (byte) 0x40);
		writeRC522(MODE_REG, (byte) 0x3D);

		antennaOn();
	}

	/**
	 * It selects a card and returns with the tagId. First it send a request, then it set up the anti-collision and
	 * finally it select the card for further operations
	 *
	 * @return The CommunicationResult object with the tagId array
	 */
	@Override
	public CommunicationResult selectCard() {
		CommunicationResult requestResult = request();
		if (!requestResult.isSuccess()) {
			return requestResult;
		}

		CommunicationResult antiCollisionResult = antiCollision();
		if (!antiCollisionResult.isSuccess()) {
			return antiCollisionResult;
		}

		byte[] tagId = antiCollisionResult.getData(TAG_ID_SIZE);

		CommunicationResult selectTagResult = selectTag(tagId);

		selectTagResult.setData(tagId);

		return selectTagResult;
	}

	/**
	 * Authenticates a given block on your already selected card. You must select your card before this process!
	 *
	 * @param authMode     Either it can be PICC_AUTHENT1A or PICC_AUTHENT1B, depends on the key type, 1 byte long
	 * @param blockAddress Target block address, 1 byte long
	 * @param key          The authentication's key, 6 bytes long
	 * @param uid          The selected cards tagId (uid), 4 bytes long
	 * @return The result of the authentication process
	 */
	@Override
	public CommunicationResult authCard(byte authMode, byte blockAddress, byte[] key, byte[] uid) {
		byte[] data = new byte[12];

		data[0] = authMode;
		data[1] = blockAddress;
		arraycopy(key, 0, data, 2, 6);
		arraycopy(uid, 0, data, 8, 4);

		CommunicationResult result = writeCard(PCD_AUTHENT, data);

		if ((readRC522(STATUS_2_REG) & 0x08) == 0) {
			result.setStatus(CommunicationStatus.AUTH_ERROR);
		}

		return result;
	}

	/**
	 * Reads data from block. You must authenticate this block before reading from it! The returned CommunicationResult
	 * object contains the block data, which is 16 bytes long!
	 *
	 * @param blockAddress The address of your target block
	 * @return The result object with the block data
	 */
	@Override
	public CommunicationResult read(byte blockAddress) {
		byte[] data = new byte[4];

		data[0] = PICC_READ;
		data[1] = blockAddress;

		calculateCRC(data);

		CommunicationResult result = writeCard(CONTROL_REG, data);

		boolean isDataLengthValid = result.getLength() == BYTE_COUNT;

		result.setStatus(isDataLengthValid ? CommunicationStatus.SUCCESS : CommunicationStatus.ERROR);

		return result;
	}

	/**
	 * Writes data to the target block. You must authenticate this block before writing to it!
	 *
	 * @param blockAddress The target block's address
	 * @param data         A 16 bytes long data array
	 * @return The result of the communication
	 */
	@Override
	public CommunicationResult write(byte blockAddress, byte[] data) {
		byte[] buff = new byte[4];
		byte[] buffWrite = new byte[data.length + 2];

		buff[0] = PICC_WRITE;
		buff[1] = blockAddress;

		calculateCRC(buff);

		CommunicationResult result = writeCard(CONTROL_REG, buff);
		if (!result.isSuccess() || result.getBits() != 4 || (result.getDataByte(0) & 0x0F) != 0x0A) {
			result.setStatus(CommunicationStatus.ERROR);

			return result;
		}

		arraycopy(data, 0, buffWrite, 0, data.length);

		calculateCRC(buffWrite);

		result = writeCard(CONTROL_REG, buffWrite);
		if (!result.isSuccess() || result.getBits() != 4 || (result.getDataByte(0) & 0x0F) != 0x0A) {
			result.setStatus(CommunicationStatus.ERROR);

			return result;
		}

		return result;
	}

	//Anti-collision detection.
	//Returns tuple of (error state, tag ID).
	private CommunicationResult antiCollision() {
		byte[] serialNumber = new byte[2];
		int serialNumberCheck = 0;

		writeRC522(BIT_FRAMING_REG, (byte) 0x00);
		serialNumber[0] = ANTICOLLISION_CL1_1;
		serialNumber[1] = ANTICOLLISION_CL1_2;

		CommunicationResult result = writeCard(CONTROL_REG, serialNumber);

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

	private CommunicationResult selectTag(byte[] uid) {
		byte[] data = new byte[9];

		data[0] = SELECT_CL1_1;
		data[1] = SELECT_CL1_2;

		for (int i = 0, j = 2; i < 5; i++, j++) {
			data[j] = uid[i];
		}

		calculateCRC(data);

		return writeCard(CONTROL_REG, data);
	}

	private CommunicationResult request() {
		byte[] tagType = new byte[1];

		writeRC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = RF_CFG_REG;

		CommunicationResult result = writeCard(CONTROL_REG, tagType);

		if (!result.isSuccess()) {
			result.setStatus(CommunicationStatus.NO_TAG);
		} else if (result.getBits() != 0x10) {
			result.setStatus(CommunicationStatus.ERROR);
		}

		return result;
	}

	private void writeRC522(byte address, byte value) {
		byte[] data = new byte[2];

		data[0] = formatAddress(false, address);
		data[1] = value;

		int responseCode = raspberryPiAdapter.wiringPiSPIDataRW(data);
		if (responseCode == -1) {
			logger.error("Device SPI write error, address={}, value={}", address, value);
		}
	}

	private byte readRC522(byte address) {
		byte[] data = new byte[2];

		data[0] = formatAddress(true, address);

		int responseCode = raspberryPiAdapter.wiringPiSPIDataRW(data);
		if (responseCode == -1) {
			logger.error("Device SPI read error, address={}", address);
		}

		return data[1];
	}

	private void calculateCRC(byte[] data) {
		clearBitMask(DIV_IRQ_REG, (byte) 0x04);
		setBitMask(FIFO_LEVEL_REG, (byte) 0x80);

		for (int i = 0; i < data.length - 2; i++) {
			writeRC522(FIFO_DATA_REG, data[i]);
		}

		writeRC522(COMMAND_REG, DIVL_EN_REG);

		int n;
		int i = 255;

		do {
			n = readRC522(DIV_IRQ_REG);
			i--;
		} while ((i != 0) && ((n & 0x04) <= 0));

		data[data.length - 2] = readRC522(CRC_RESULT_REG_LSB);
		data[data.length - 1] = readRC522(CRC_RESULT_REG_MSB);
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
		} else if (command == CONTROL_REG) {
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
		if (command == CONTROL_REG)
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

			if (command == CONTROL_REG) {
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

	/**
	 * The address has a special format:
	 * positions:	7					6	5	4	3	2	1	0
	 * values:		1/0 (read/write)	6-1 address bits		0
	 *
	 * @param read    Read or write?
	 * @param address The original address
	 * @return The formatted address
	 */
	private byte formatAddress(boolean read, byte address) {
		if (read) {
			return (byte) (((address << 1) & 0x7E) | 0x80);
		} else {
			return (byte) ((address << 1) & 0x7E);
		}
	}

}
