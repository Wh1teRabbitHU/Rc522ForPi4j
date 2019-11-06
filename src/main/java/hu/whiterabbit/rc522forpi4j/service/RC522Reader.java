package hu.whiterabbit.rc522forpi4j.service;

import com.pi4j.wiringpi.Spi;
import hu.whiterabbit.rc522forpi4j.model.ReadResult;
import java.nio.charset.StandardCharsets;

import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class RC522Reader {

	private static final RC522Communicator rc522 = new RC522Communicator();

	public String readTag() {
		byte[] tagId = new byte[5];

		int readStatus = rc522.selectMirareOne(tagId);

		if (readStatus == 2) {
			return "";
		}

		return bytesToHex(tagId);
	}

	public ReadResult readBlock(int block) {
		return null;
	}

	public ReadResult readSector(int fromBlock, int toBlock) {
		return null;
	}

	public ReadResult readAllData() {
		return null;
	}

	public void read() throws InterruptedException {
		byte[] tagId = new byte[5];

		int readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == 2) {
			return;
		}

		String strUID = bytesToHex(tagId);

		System.out.println("Card Read UID: (HEX) " + strUID);

		for (byte sector = 0; sector < 16; sector++) {
			for (byte block = 0; block < 4; block++) {
				authAndReadData(sector, block, tagId);
			}
		}

		System.out.println("Read ended");

		Thread.sleep(3000);


		/*
		//default key
		byte[] keyA = new byte[] { (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 };
		byte[] keyB = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

		//Authenticate,A密钥验证卡,可以读数据块2
		byte data[] = new byte[16];
		status = rc522.Auth_Card(RC522Reader.PICC_AUTHENT1A, sector, block, keyA, tagid);
		if (status != RC522Reader.MI_OK) {
			System.out.println("Authenticate A error");
			return;
		}

		status = rc522.Read(sector, block, data);
		//rc522.Stop_Crypto();
		System.out.println("Successfully authenticated,Read data=" + bytesToHex(data));
		status = rc522.Read(sector, (byte) 3, data);
		System.out.println("Read control block data=" + bytesToHex(data));


		for (i = 0; i < 16; i++) {
			data[i] = (byte) 0x00;
		}

		//Authenticate,B密钥验证卡,可以写数据块2
		status = rc522.Auth_Card(RC522Reader.PICC_AUTHENT1B, sector, block, keyB, tagid);
		if (status != RC522Reader.MI_OK) {
			System.out.println("Authenticate B error");
			return;
		}

		status = rc522.Write(sector, block, data);
		if (status == RC522Reader.MI_OK)
			System.out.println("Write data finished");
		else {
			System.out.println("Write data error,status=" + status);
			return;
		}

		byte[] buff = new byte[16];

		for (i = 0; i < 16; i++) {
			buff[i] = (byte) 0;
		}
		status = rc522.Read(sector, block, buff);
		if (status == RC522Reader.MI_OK)
			System.out.println("Read Data finished");
		else {
			System.out.println("Read data error,status=" + status);
			return;
		}

		System.out.print("sector" + sector + ",block=" + block + " :");
		String strData = bytesToHex(buff);
		for (i = 0; i < 16; i++) {
			System.out.print(strData.substring(i * 2, i * 2 + 2));
			if (i < 15) System.out.print(",");
			else System.out.println("");
		}*/
	}

	public void read2() throws InterruptedException {
		byte[] tagid = new byte[5];

		int readStatus = rc522.selectMirareOne(tagid);
		if (readStatus == 2) {
			return;
		}

		System.out.println("Card Read UID: " + bytesToHex(tagid));

		Thread.sleep(2000);
	}

	public void loop() throws InterruptedException {
		int count = 0;
		while (count++ < 3) {

			int packetlength = 5;

			byte packet[] = new byte[packetlength];
			packet[0] = (byte) 0x80; // FIRST PACKET GETS IGNORED BUT HAS TO BE SET TO READ
			packet[1] = (byte) 0x80; // ADDRESS 0 Gives data of Address 0
			packet[2] = (byte) 0x82; // ADDRESS 1 Gives data of Address 1
			packet[3] = (byte) 0x84; // ADDRESS 2 Gives data of Address 2
			packet[4] = (byte) 0x86; // ADDRESS 3 Gives data of Address 3

			System.out.println("-----------------------------------------------");
			System.out.println("Data to be transmitted:");
			System.out.println("[TX] " + bytesToHex(packet));
			System.out.println("[TX1] " + packet[1]);
			System.out.println("[TX2] " + packet[2]);
			System.out.println("[TX3] " + packet[3]);
			System.out.println("[TX4] " + packet[4]);
			System.out.println("Transmitting data...");

			// Send data to Reader and receive answerpacket.
			packet = readFromRFID(0, packet, packetlength);

			System.out.println("Data transmitted, packets received.");
			System.out.println("Received Packets (First packet to be ignored!)");
			System.out.println("[RX] " + bytesToHex(packet));
			System.out.println("[RX1] " + packet[1]);
			System.out.println("[RX2] " + packet[2]);
			System.out.println("[RX3] " + packet[3]);
			System.out.println("[RX4] " + packet[4]);
			System.out.println("-----------------------------------------------");

			if (packet.length == 0) {
				//Reset when no packet received
				//ResetPin.high();
				Thread.sleep(50);
				//ResetPin.low();
			}
		}
	}

	private String authAAndReadData(byte sector, byte block, byte[] tagId) {
		System.out.println("Authenticate A");

		byte[] keyA = new byte[] { (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 };
		int status = rc522.authCard(RC522Communicator.PICC_AUTHENT1A, sector, block, keyA, tagId);

		if (status != RC522Communicator.MI_OK) {
			System.out.println("Authentication error");

			return "";
		}

		return readData(sector, block);
	}

	private String authAndReadData(byte sector, byte block, byte[] tagId) {
		System.out.println("Authenticate...");

		byte[] keyB = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		int status = rc522.authCard(RC522Communicator.PICC_AUTHENT1A, sector, block, keyB, tagId);

		if (status != RC522Communicator.MI_OK) {
			System.out.println("Authentication error");

			return "";
		}

		return readData(sector, block);
	}

	private String readData(byte sector, byte block) {
		byte[] buff = new byte[16];

		for (int i = 0; i < 16; i++) {
			buff[i] = (byte) 0;
		}
		int status = rc522.read(sector, block, buff);

		System.out.print("sector = " + sector + ", block = " + block + ": ");

		if (status != RC522Communicator.MI_OK) {
			System.out.println("");
			return "";
		}

		String strData = new String(buff, StandardCharsets.US_ASCII);

		System.out.println(strData);

		return strData;
	}

	private byte[] readFromRFID(int channel, byte[] packet, int length) {
		Spi.wiringPiSPIDataRW(channel, packet, length);

		return packet;
	}

	private boolean writeToRFID(int channel, byte fullAddress, byte data) {

		byte[] packet = new byte[2];
		packet[0] = fullAddress;
		packet[1] = data;

		if (Spi.wiringPiSPIDataRW(channel, packet, 1) >= 0)
			return true;
		else
			return false;
	}

}
