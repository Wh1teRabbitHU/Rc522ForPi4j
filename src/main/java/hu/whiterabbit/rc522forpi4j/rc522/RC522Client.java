package hu.whiterabbit.rc522forpi4j.rc522;

import com.pi4j.wiringpi.Spi;
import hu.whiterabbit.rc522forpi4j.model.CommunicationResult;
import hu.whiterabbit.rc522forpi4j.model.CommunicationStatus;

import java.nio.charset.StandardCharsets;

import static hu.whiterabbit.rc522forpi4j.rc522.RC522CommandTable.PICC_AUTHENT1A;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class RC522Client {

	private static final int RESET_PIN = 22;

	private static final int SPEED = 500000;

	private static final int SPI_CHANNEL = 0;

	private static final RC522Adapter rc522 = new RC522Adapter(SPEED, RESET_PIN, SPI_CHANNEL);

	public String readTag() {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
			return "";
		}

		return bytesToHex(tagId);
	}

	public void read() throws InterruptedException {
		byte[] tagId = new byte[5];

		CommunicationStatus readStatus = rc522.selectMirareOne(tagId);
		if (readStatus == CommunicationStatus.ERROR) {
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

		Thread.sleep(1000);

		System.out.println("sleep ended");

		rc522.reset();
		Thread.sleep(50);


		/*
		//default key
		byte[] keyA = new byte[] { (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 };
		byte[] keyB = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

		//Authenticate
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

		//Authenticate
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

	/*
	private String authAAndReadData(byte sector, byte block, byte[] tagId) {
		System.out.println("Authenticate A");

		byte[] keyA = new byte[]{(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03};
		CommunicationStatus status = rc522.authCard(PICC_AUTHENT1A, sector, block, keyA, tagId);

		if (status != CommunicationStatus.SUCCESS) {
			System.out.println("Authentication error");

			return "";
		}

		return readData(sector, block);
	}
	*/

	private String authAndReadData(byte sector, byte block, byte[] tagId) {
		System.out.println("Authenticate...");

		byte[] keyB = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
		CommunicationStatus status = rc522.authCard(PICC_AUTHENT1A, sector, block, keyB, tagId);

		if (status != CommunicationStatus.SUCCESS) {
			System.out.println("Authentication error");

			return "";
		}

		return readData(sector, block);
	}

	private String readData(byte sector, byte block) {
		CommunicationResult result = rc522.read(sector, block);

		System.out.print("sector = " + sector + ", block = " + block + ": ");

		if (!result.isSuccess()) {
			System.out.println("");
			return "";
		}

		String strData = new String(result.getData(), StandardCharsets.ISO_8859_1);

		System.out.println(strData);

		return strData;
	}

}
