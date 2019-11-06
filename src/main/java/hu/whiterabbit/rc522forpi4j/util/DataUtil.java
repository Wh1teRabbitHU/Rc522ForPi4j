package hu.whiterabbit.rc522forpi4j.util;

import hu.whiterabbit.rc522forpi4j.model.CommunicationStatus;

public class DataUtil {

	private DataUtil() {
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'A', 'B', 'C', 'D', 'E', 'F'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static CommunicationStatus getStatus(int statusCode) {
		switch (statusCode) {
			case 0:
				return CommunicationStatus.SUCCESS;
			case 1:
				return CommunicationStatus.NO_TAG;
			case 2:
			default:
				return CommunicationStatus.ERROR;
		}
	}

}
