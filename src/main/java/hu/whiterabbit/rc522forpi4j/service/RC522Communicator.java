package hu.whiterabbit.rc522forpi4j.service;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;
import hu.whiterabbit.rc522forpi4j.model.ReadResult;
import hu.whiterabbit.rc522forpi4j.model.RequestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.whiterabbit.rc522forpi4j.util.CommandUtil.*;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.getStatus;

@SuppressWarnings({"unused", "WeakerAccess"})
public class RC522Communicator {

	private static final Logger logger = LoggerFactory.getLogger(RC522Communicator.class);

	private final int resetPin;        //RST Pin number,default 22

	private final int speed;

	private static final int SPI_Channel = 0;

	private static final int MAX_LEN = 16; //扇区字节数

	public RC522Communicator() {
		this.resetPin = 22;
		this.speed = 500000;

		init();
	}

	public RC522Communicator(int speed, int resetPin) {
		this.resetPin = resetPin;
		this.speed = speed;

		if (speed < 500000 || speed > 32000000) {
			logger.error("Speed out of range: {}", speed);

			return;
		}

		init();
	}

	private void init() {
		Gpio.wiringPiSetup();           //Enable wiringPi pin schema
		int fd = Spi.wiringPiSPISetup(SPI_Channel, speed);
		if (fd <= -1) {
			logger.error(" --> Failed to set up  SPI communication");
			//Stop code when error happened
			return;
		} else {
			logger.info(" --> Successfully loaded SPI communication");
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
		int result = Spi.wiringPiSPIDataRW(SPI_Channel, data);
		if (result == -1)
			System.out.println("Device write  error,address=" + address + ",value=" + value);
	}

	private byte readRC522(byte address) {
		byte[] data = new byte[2];
		data[0] = (byte) (((address << 1) & 0x7E) | 0x80);
		int result = Spi.wiringPiSPIDataRW(SPI_Channel, data);
		if (result == -1)
			System.out.println("Device read  error,address=" + address);
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
		//   if((value & 0x03) != 0x03)
		setBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private void antennaOff() {
		clearBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private int writeCard(byte command, byte[] data, int dataLen, byte[] back_data, int[] back_bits, int[] backLen) {
		int status = MI_ERR;
		byte irq = 0, irq_wait = 0, lastBits;
		int n;
		int i;

		backLen[0] = 0;
		if (command == PCD_AUTHENT) {
			irq = 0x12;
			irq_wait = 0x10;
		} else if (command == PCD_TRANSCEIVE) {
			irq = 0x77;
			irq_wait = 0x30;
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
		//System.out.println("Write_Card i="+i+",n="+n);
		do {
			n = readRC522(COMM_IRQ_REG);
			i--;
		} while ((i != 0) && (n & 0x01) <= 0 && (n & irq_wait) <= 0);

		clearBitMask(BIT_FRAMING_REG, (byte) 0x80);

		if (i != 0) {
			if ((readRC522(ERROR_REG) & 0x1B) == 0x00) {
				status = MI_OK;
				if ((n & irq & 0x01) > 0)
					status = MI_NOTAGERR;
				if (command == PCD_TRANSCEIVE) {
					n = readRC522(FIFO_LEVEL_REG);
					lastBits = (byte) (readRC522(CONTROL_REG) & 0x07);
					if (lastBits != 0)
						back_bits[0] = (n - 1) * 8 + lastBits;
					else
						back_bits[0] = n * 8;

					if (n == 0) n = 1;
					if (n > MAX_LEN) n = MAX_LEN;
					backLen[0] = n;
					for (i = 0; i < n; i++)
						back_data[i] = readRC522(FIFO_DATA_REG);
				}
			}
		}

		return status;
	}

	public int request(byte req_mode, int[] back_bits) {
		int status;
		byte[] tagType = new byte[1];
		byte[] data_back = new byte[16];
		int[] backLen = new int[1];

		writeRC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = req_mode;
		back_bits[0] = 0;
		status = writeCard(PCD_TRANSCEIVE, tagType, 1, data_back, back_bits, backLen);
		if (status != MI_OK || back_bits[0] != 0x10) {
			//System.out.println("status="+status+",back_bits[0]="+back_bits[0]);
			status = MI_ERR;
		}

		return status;
	}

	public RequestResult request(byte req_mode) {
		byte[] tagType = new byte[1];
		byte[] data_back = new byte[16];
		int[] back_bits = new int[1];
		int[] backLen = new int[1];

		writeRC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = req_mode;
		int status = writeCard(PCD_TRANSCEIVE, tagType, 1, data_back, back_bits, backLen);

		if (status != MI_OK || back_bits[0] != 0x10) {
			status = MI_ERR;
		}

		return new RequestResult(getStatus(status));
	}

	//Anti-collision detection.
	//Returns tuple of (error state, tag ID).
	public int antiColl(byte[] back_data) {
		byte[] serial_number = new byte[2];   //2字节命令
		int serial_number_check = 0;
		int[] backLen = new int[1];
		int[] back_bits = new int[1];

		writeRC522(BIT_FRAMING_REG, (byte) 0x00);
		serial_number[0] = PICC_ANTICOLL;
		serial_number[1] = 0x20;
		int status = writeCard(PCD_TRANSCEIVE, serial_number, 2, back_data, back_bits, backLen);
		if (status == MI_OK) {
			if (backLen[0] == 5) {
				for (int i = 0; i < 4; i++)
					serial_number_check ^= back_data[i];
				if (serial_number_check != back_data[4]) {
					status = MI_ERR;
					System.out.println("check error");
				}
			} else {
				System.out.println("backLen[0]=" + backLen[0]);
			}
		}
		return status;
	}

	public RequestResult antiColl() {
		int status;
		byte[] serial_number = new byte[2];   //2字节命令
		int serial_number_check = 0;
		int[] backLen = new int[1];
		int[] back_bits = new int[1];
		byte[] tagData = new byte[5];
		int i;

		writeRC522(BIT_FRAMING_REG, (byte) 0x00);
		serial_number[0] = PICC_ANTICOLL;
		serial_number[1] = 0x20;
		status = writeCard(PCD_TRANSCEIVE, serial_number, 2, tagData, back_bits, backLen);

		if (status == MI_OK) {
			if (backLen[0] == 5) {
				for (i = 0; i < 4; i++)
					serial_number_check ^= tagData[i];
				if (serial_number_check != tagData[4]) {
					status = MI_ERR;
					System.out.println("check error");
				}
			} else {
				System.out.println("backLen[0]=" + backLen[0]);
			}
		}

		return new RequestResult(getStatus(status), tagData);
	}

	private void calculateCRC(byte[] data) {
		int i, n;
		clearBitMask(DIV_IRQ_REG, (byte) 0x04);
		setBitMask(FIFO_LEVEL_REG, (byte) 0x80);

		for (i = 0; i < data.length - 2; i++)
			writeRC522(FIFO_DATA_REG, data[i]);
		writeRC522(COMMAND_REG, PCD_CALCCRC);
		i = 255;
		do {
			n = readRC522(DIV_IRQ_REG);
			i--;
		} while ((i != 0) && ((n & 0x04) <= 0));

		data[data.length - 2] = readRC522(CRC_RESULT_REG_L);
		data[data.length - 1] = readRC522(CRC_RESULT_REG_M);
	}

	public int selectTag(byte[] uid) {
		int status;
		byte[] data = new byte[9];
		byte[] back_data = new byte[MAX_LEN];
		int[] back_bits = new int[1];
		int[] backLen = new int[1];
		int i, j;

		data[0] = PICC_SElECTTAG;
		data[1] = 0x70;
		for (i = 0, j = 2; i < 5; i++, j++)
			data[j] = uid[i];
		calculateCRC(data);

		status = writeCard(PCD_TRANSCEIVE, data, 9, back_data, back_bits, backLen);
		if (status == MI_OK && back_bits[0] == 0x18) return back_data[0];
		else return 0;
	}

	//Authenticates to use specified block address. Tag must be selected using select_tag(uid) before auth.
	//auth_mode-RFID.auth_a or RFID.auth_b
	//block_address- used to authenticate
	//key-list or tuple(数组) with six bytes key
	//uid-list or tuple with four bytes tag ID
	public int authCard(byte auth_mode, byte block_address, byte[] key, byte[] uid) {
		int status;
		byte[] data = new byte[12];
		byte[] back_data = new byte[MAX_LEN];
		int[] back_bits = new int[1];
		int[] backLen = new int[1];
		int i, j;

		data[0] = auth_mode;
		data[1] = block_address;
		for (i = 0, j = 2; i < 6; i++, j++)
			data[j] = key[i];
		for (i = 0, j = 8; i < 4; i++, j++)
			data[j] = uid[i];

		status = writeCard(PCD_AUTHENT, data, 12, back_data, back_bits, backLen);
		if ((readRC522(STATUS_2_REG) & 0x08) == 0) status = MI_ERR;
		return status;
	}

	//
	public int authCard(byte auth_mode, byte sector, byte block, byte[] key, byte[] uid) {
		return authCard(auth_mode, sector2BlockAddress(sector, block), key, uid);
	}

	//Ends operations with Crypto1 usage.
	public void stopCrypto() {
		clearBitMask(STATUS_2_REG, (byte) 0x08);
	}

	//Reads data from block. You should be authenticated before calling read.
	//Returns tuple of (result state, read data).
	//block_address
	//back_data-data to be read,16 bytes
	public int read(byte block_address, byte[] back_data) {
		int status;
		byte[] data = new byte[4];
		int[] back_bits = new int[1];
		int[] backLen = new int[1];
		int i, j;

		data[0] = PICC_READ;
		data[1] = block_address;
		calculateCRC(data);
		status = writeCard(PCD_TRANSCEIVE, data, data.length, back_data, back_bits, backLen);
		if (backLen[0] == 16) status = MI_OK;
		return status;
	}

	//
	public int read(byte sector, byte block, byte[] back_data) {
		return read(sector2BlockAddress(sector, block), back_data);
	}

	//Writes data to block. You should be authenticated before calling write.
	//Returns error state.
	//data-16 bytes
	public int write(byte block_address, byte[] data) {
		int status;
		byte[] buff = new byte[4];
		byte[] buff_write = new byte[data.length + 2];
		byte[] back_data = new byte[MAX_LEN];
		int[] back_bits = new int[1];
		int[] backLen = new int[1];
		int i;

		buff[0] = PICC_WRITE;
		buff[1] = block_address;
		calculateCRC(buff);
		status = writeCard(PCD_TRANSCEIVE, buff, buff.length, back_data, back_bits, backLen);
		//System.out.println("write_card  status="+status);
		//System.out.println("back_bits[0]="+back_bits[0]+",(back_data[0] & 0x0F)="+(back_data[0] & 0x0F));
		if (status != MI_OK || back_bits[0] != 4 || (back_data[0] & 0x0F) != 0x0A) status = MI_ERR;
		if (status == MI_OK) {
			for (i = 0; i < data.length; i++)
				buff_write[i] = data[i];
			calculateCRC(buff_write);
			status = writeCard(PCD_TRANSCEIVE, buff_write, buff_write.length, back_data, back_bits, backLen);
			//System.out.println("write_card data status="+status);
			//System.out.println("back_bits[0]="+back_bits[0]+",(back_data[0] & 0x0F)="+(back_data[0] & 0x0F));
			if (status != MI_OK || back_bits[0] != 4 || (back_data[0] & 0x0F) != 0x0A) status = MI_ERR;
		}
		return status;
	}

	//
	public int write(byte sector, byte block, byte[] data) {
		return write(sector2BlockAddress(sector, block), data);
	}

	public byte[] dumpClassic1K(byte[] key, byte[] uid) {
		int i, status;
		byte[] data = new byte[1024];
		byte[] buff = new byte[16];

		for (i = 0; i < 64; i++) {
			status = authCard(PICC_AUTHENT1A, (byte) i, key, uid);
			if (status == MI_OK) {
				status = read((byte) i, buff);
				if (status == MI_OK)
					System.arraycopy(buff, 0, data, i * 64, 16);
			}
		}

		return data;
	}

	//Convert sector  to blockaddress
	//sector-0~15
	//block-0~3
	//return blockaddress
	private byte sector2BlockAddress(byte sector, byte block) {
		if (sector < 0 || sector > 15 || block < 0 || block > 3) return (byte) (-1);
		return (byte) (sector * 4 + block);
	}

	//uid-5 bytes
	public int selectMirareOne(byte[] uid) {
		int[] back_bits = new int[1];
		byte[] tagid = new byte[5];
		int status;

		status = request(PICC_REQIDL, back_bits);
		if (status != MI_OK) return status;
		status = antiColl(tagid);
		if (status != MI_OK) return status;
		selectTag(tagid);
		System.arraycopy(tagid, 0, uid, 0, 5);

		return status;
	}

	public ReadResult tryToReadCardTag() {
		int[] back_bits = new int[1];
		byte[] tagid = new byte[5];
		RequestResult status;

		status = request(PICC_REQIDL);
		if (!status.isSuccess()) {
			return new ReadResult(status);
		}
		status = antiColl();
		if (!status.isSuccess()) {
			return new ReadResult(status);
		}

		selectTag(status.getResponseData());

		return new ReadResult(status);
	}

}
