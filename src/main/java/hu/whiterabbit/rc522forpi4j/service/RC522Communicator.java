package hu.whiterabbit.rc522forpi4j.service;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;
import hu.whiterabbit.rc522forpi4j.model.ReadResult;
import hu.whiterabbit.rc522forpi4j.model.RequestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.whiterabbit.rc522forpi4j.util.CommandUtil.*;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.getStatus;

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
		Reset();
		Write_RC522(T_MODE_REG, (byte) 0x8D);
		Write_RC522(T_PRESCALER_REG, (byte) 0x3E);
		Write_RC522(T_RELOAD_REG_L, (byte) 30);
		Write_RC522(T_RELOAD_REG_H, (byte) 0);
		Write_RC522(TX_AUTO_REG, (byte) 0x40);
		Write_RC522(MODE_REG, (byte) 0x3D);
		AntennaOn();
	}

	private void Reset() {
		Write_RC522(COMMAND_REG, PCD_RESETPHASE);
	}

	private void Write_RC522(byte address, byte value) {
		byte[] data = new byte[2];
		data[0] = (byte) ((address << 1) & 0x7E);
		data[1] = value;
		int result = Spi.wiringPiSPIDataRW(SPI_Channel, data);
		if (result == -1)
			System.out.println("Device write  error,address=" + address + ",value=" + value);
	}

	private byte Read_RC522(byte address) {
		byte data[] = new byte[2];
		data[0] = (byte) (((address << 1) & 0x7E) | 0x80);
		data[1] = 0;
		int result = Spi.wiringPiSPIDataRW(SPI_Channel, data);
		if (result == -1)
			System.out.println("Device read  error,address=" + address);
		return data[1];
	}

	private void SetBitMask(byte address, byte mask) {
		byte value = Read_RC522(address);
		Write_RC522(address, (byte) (value | mask));
	}

	private void ClearBitMask(byte address, byte mask) {
		byte value = Read_RC522(address);
		Write_RC522(address, (byte) (value & (~mask)));
	}

	private void AntennaOn() {
		byte value = Read_RC522(TX_CONTROL_REG);
		//   if((value & 0x03) != 0x03)
		SetBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	private void AntennaOff() {
		ClearBitMask(TX_CONTROL_REG, (byte) 0x03);
	}

	//back_data-最长不超过Length=16;
	//back_data-返回数据
	//back_bits-返回比特数
	//backLen-返回字节数
	private int Write_Card(byte command, byte[] data, int dataLen, byte[] back_data, int[] back_bits, int[] backLen) {
		int status = MI_ERR;
		byte irq = 0, irq_wait = 0, lastBits = 0;
		int n = 0, i = 0;

		backLen[0] = 0;
		if (command == PCD_AUTHENT) {
			irq = 0x12;
			irq_wait = 0x10;
		} else if (command == PCD_TRANSCEIVE) {
			irq = 0x77;
			irq_wait = 0x30;
		}

		Write_RC522(COMM_I_EN_REG, (byte) (irq | 0x80));
		ClearBitMask(COMM_IRQ_REG, (byte) 0x80);
		SetBitMask(FIFO_LEVEL_REG, (byte) 0x80);

		Write_RC522(COMMAND_REG, PCD_IDLE);

		for (i = 0; i < dataLen; i++)
			Write_RC522(FIFO_DATA_REG, data[i]);

		Write_RC522(COMMAND_REG, command);
		if (command == PCD_TRANSCEIVE)
			SetBitMask(BIT_FRAMING_REG, (byte) 0x80);

		i = 2000;
		while (true) {
			n = Read_RC522(COMM_IRQ_REG);
			i--;
			if ((i == 0) || (n & 0x01) > 0 || (n & irq_wait) > 0) {
				//System.out.println("Write_Card i="+i+",n="+n);
				break;
			}
		}
		ClearBitMask(BIT_FRAMING_REG, (byte) 0x80);

		if (i != 0) {
			if ((Read_RC522(ERROR_REG) & 0x1B) == 0x00) {
				status = MI_OK;
				if ((n & irq & 0x01) > 0)
					status = MI_NOTAGERR;
				if (command == PCD_TRANSCEIVE) {
					n = Read_RC522(FIFO_LEVEL_REG);
					lastBits = (byte) (Read_RC522(CONTROL_REG) & 0x07);
					if (lastBits != 0)
						back_bits[0] = (n - 1) * 8 + lastBits;
					else
						back_bits[0] = n * 8;

					if (n == 0) n = 1;
					if (n > this.MAX_LEN) n = this.MAX_LEN;
					backLen[0] = n;
					for (i = 0; i < n; i++)
						back_data[i] = Read_RC522(FIFO_DATA_REG);
				}
			} else
				status = MI_ERR;
		}
		return status;
	}

	public int Request(byte req_mode, int[] back_bits) //参数为1字节数组
	{
		int status;
		byte tagType[] = new byte[1];
		byte data_back[] = new byte[16];
		int backLen[] = new int[1];

		Write_RC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = req_mode;
		back_bits[0] = 0;
		status = Write_Card(PCD_TRANSCEIVE, tagType, 1, data_back, back_bits, backLen);
		if (status != MI_OK || back_bits[0] != 0x10) {
			//System.out.println("status="+status+",back_bits[0]="+back_bits[0]);
			status = MI_ERR;
		}

		return status;
	}

	public RequestResult Request(byte req_mode) {
		int status;
		byte[] tagType = new byte[1];
		byte[] data_back = new byte[16];
		int[] back_bits = new int[1];
		int[] backLen = new int[1];

		Write_RC522(BIT_FRAMING_REG, (byte) 0x07);

		tagType[0] = req_mode;
		back_bits[0] = 0;
		status = Write_Card(PCD_TRANSCEIVE, tagType, 1, data_back, back_bits, backLen);

		if (status != MI_OK || back_bits[0] != 0x10) {
			status = MI_ERR;
		}

		return new RequestResult(getStatus(status));
	}

	//Anti-collision detection.
	//Returns tuple of (error state, tag ID).
	//back_data-5字节 4字节tagid+1字节校验
	public int AntiColl(byte[] back_data) {
		int status;
		byte[] serial_number = new byte[2];   //2字节命令
		int serial_number_check = 0;
		int backLen[] = new int[1];
		int back_bits[] = new int[1];
		int i;

		Write_RC522(BIT_FRAMING_REG, (byte) 0x00);
		serial_number[0] = PICC_ANTICOLL;
		serial_number[1] = 0x20;
		status = Write_Card(PCD_TRANSCEIVE, serial_number, 2, back_data, back_bits, backLen);
		if (status == MI_OK) {
			if (backLen[0] == 5) {
				for (i = 0; i < 4; i++)
					serial_number_check ^= back_data[i];
				if (serial_number_check != back_data[4]) {
					status = MI_ERR;
					System.out.println("check error");
				}
			} else {
				status = MI_OK;
				System.out.println("backLen[0]=" + backLen[0]);
			}
		}
		return status;
	}

	public RequestResult AntiColl() {
		int status;
		byte[] serial_number = new byte[2];   //2字节命令
		int serial_number_check = 0;
		int backLen[] = new int[1];
		int back_bits[] = new int[1];
		byte[] tagData = new byte[5];
		int i;

		Write_RC522(BIT_FRAMING_REG, (byte) 0x00);
		serial_number[0] = PICC_ANTICOLL;
		serial_number[1] = 0x20;
		status = Write_Card(PCD_TRANSCEIVE, serial_number, 2, tagData, back_bits, backLen);
		if (status == MI_OK) {
			if (backLen[0] == 5) {
				for (i = 0; i < 4; i++)
					serial_number_check ^= tagData[i];
				if (serial_number_check != tagData[4]) {
					status = MI_ERR;
					System.out.println("check error");
				}
			} else {
				status = MI_OK;
				System.out.println("backLen[0]=" + backLen[0]);
			}
		}
		return new RequestResult(getStatus(status), tagData);
	}

	//CRC值放在data[]最后两字节
	private void Calculate_CRC(byte[] data) {
		int i, n;
		ClearBitMask(DIV_IRQ_REG, (byte) 0x04);
		SetBitMask(FIFO_LEVEL_REG, (byte) 0x80);

		for (i = 0; i < data.length - 2; i++)
			Write_RC522(FIFO_DATA_REG, data[i]);
		Write_RC522(COMMAND_REG, PCD_CALCCRC);
		i = 255;
		while (true) {
			n = Read_RC522(DIV_IRQ_REG);
			i--;
			if ((i == 0) || ((n & 0x04) > 0))
				break;
		}
		data[data.length - 2] = Read_RC522(CRC_RESULT_REG_L);
		data[data.length - 1] = Read_RC522(CRC_RESULT_REG_M);
	}

	//uid-5字节数组,存放序列号
	//返值是大小
	public int Select_Tag(byte[] uid) {
		int status;
		byte data[] = new byte[9];
		byte back_data[] = new byte[this.MAX_LEN];
		int back_bits[] = new int[1];
		int backLen[] = new int[1];
		int i, j;

		data[0] = PICC_SElECTTAG;
		data[1] = 0x70;
		for (i = 0, j = 2; i < 5; i++, j++)
			data[j] = uid[i];
		Calculate_CRC(data);

		status = Write_Card(PCD_TRANSCEIVE, data, 9, back_data, back_bits, backLen);
		if (status == MI_OK && back_bits[0] == 0x18) return back_data[0];
		else return 0;
	}

	//Authenticates to use specified block address. Tag must be selected using select_tag(uid) before auth.
	//auth_mode-RFID.auth_a or RFID.auth_b
	//block_address- used to authenticate
	//key-list or tuple(数组) with six bytes key
	//uid-list or tuple with four bytes tag ID
	public int Auth_Card(byte auth_mode, byte block_address, byte[] key, byte[] uid) {
		int status;
		byte data[] = new byte[12];
		byte back_data[] = new byte[this.MAX_LEN];
		int back_bits[] = new int[1];
		int backLen[] = new int[1];
		int i, j;

		data[0] = auth_mode;
		data[1] = block_address;
		for (i = 0, j = 2; i < 6; i++, j++)
			data[j] = key[i];
		for (i = 0, j = 8; i < 4; i++, j++)
			data[j] = uid[i];

		status = Write_Card(PCD_AUTHENT, data, 12, back_data, back_bits, backLen);
		if ((Read_RC522(STATUS_2_REG) & 0x08) == 0) status = MI_ERR;
		return status;
	}

	//
	public int Auth_Card(byte auth_mode, byte sector, byte block, byte[] key, byte[] uid) {
		return Auth_Card(auth_mode, Sector2BlockAddress(sector, block), key, uid);
	}

	//Ends operations with Crypto1 usage.
	public void Stop_Crypto() {
		ClearBitMask(STATUS_2_REG, (byte) 0x08);
	}

	//Reads data from block. You should be authenticated before calling read.
	//Returns tuple of (result state, read data).
	//block_address
	//back_data-data to be read,16 bytes
	public int Read(byte block_address, byte[] back_data) {
		int status;
		byte data[] = new byte[4];
		int back_bits[] = new int[1];
		int backLen[] = new int[1];
		int i, j;

		data[0] = PICC_READ;
		data[1] = block_address;
		Calculate_CRC(data);
		status = Write_Card(PCD_TRANSCEIVE, data, data.length, back_data, back_bits, backLen);
		if (backLen[0] == 16) status = MI_OK;
		return status;
	}

	//
	public int Read(byte sector, byte block, byte[] back_data) {
		return Read(Sector2BlockAddress(sector, block), back_data);
	}

	//Writes data to block. You should be authenticated before calling write.
	//Returns error state.
	//data-16 bytes
	public int Write(byte block_address, byte[] data) {
		int status;
		byte buff[] = new byte[4];
		byte buff_write[] = new byte[data.length + 2];
		byte back_data[] = new byte[this.MAX_LEN];
		int back_bits[] = new int[1];
		int backLen[] = new int[1];
		int i;

		buff[0] = PICC_WRITE;
		buff[1] = block_address;
		Calculate_CRC(buff);
		status = Write_Card(PCD_TRANSCEIVE, buff, buff.length, back_data, back_bits, backLen);
		//System.out.println("write_card  status="+status);
		//System.out.println("back_bits[0]="+back_bits[0]+",(back_data[0] & 0x0F)="+(back_data[0] & 0x0F));
		if (status != MI_OK || back_bits[0] != 4 || (back_data[0] & 0x0F) != 0x0A) status = MI_ERR;
		if (status == MI_OK) {
			for (i = 0; i < data.length; i++)
				buff_write[i] = data[i];
			Calculate_CRC(buff_write);
			status = Write_Card(PCD_TRANSCEIVE, buff_write, buff_write.length, back_data, back_bits, backLen);
			//System.out.println("write_card data status="+status);
			//System.out.println("back_bits[0]="+back_bits[0]+",(back_data[0] & 0x0F)="+(back_data[0] & 0x0F));
			if (status != MI_OK || back_bits[0] != 4 || (back_data[0] & 0x0F) != 0x0A) status = MI_ERR;
		}
		return status;
	}

	//
	public int Write(byte sector, byte block, byte[] data) {
		return Write(Sector2BlockAddress(sector, block), data);
	}

	//导出1K字节,64个扇区
	public byte[] DumpClassic1K(byte[] key, byte[] uid) {
		int i, status;
		byte[] data = new byte[1024];
		byte[] buff = new byte[16];

		for (i = 0; i < 64; i++) {
			status = Auth_Card(PICC_AUTHENT1A, (byte) i, key, uid);
			if (status == MI_OK) {
				status = Read((byte) i, buff);
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
	private byte Sector2BlockAddress(byte sector, byte block) {
		if (sector < 0 || sector > 15 || block < 0 || block > 3) return (byte) (-1);
		return (byte) (sector * 4 + block);
	}

	//uid-5 bytes
	public int Select_MirareOne(byte[] uid) {
		int[] back_bits = new int[1];
		byte[] tagid = new byte[5];
		int status;

		status = Request(PICC_REQIDL, back_bits);
		if (status != MI_OK) return status;
		status = AntiColl(tagid);
		if (status != MI_OK) return status;
		Select_Tag(tagid);
		System.arraycopy(tagid, 0, uid, 0, 5);

		return status;
	}

	public ReadResult tryToReadCardTag() {
		int[] back_bits = new int[1];
		byte[] tagid = new byte[5];
		RequestResult status;

		status = Request(PICC_REQIDL);
		if (!status.isSuccess()) {
			return new ReadResult(status);
		}
		status = AntiColl();
		if (!status.isSuccess()) {
			return new ReadResult(status);
		}

		Select_Tag(status.getResponseData());

		return new ReadResult(status);
	}

}
