package hu.whiterabbit.rc522forpi4j.util;

import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

public class DataUtil {

	private static final char[] HEX_ARRAY = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	private DataUtil() {
	}

	public static String byteToHex(byte byteValue) {
		int v = byteValue & 0xFF;
		return HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F];
	}

	public static String bytesToHex(byte[] bytes) {
		return bytesToHex(bytes, (a, b) -> a + b);
	}

	public static String bytesToHex(byte[] bytes, BinaryOperator<String> reduceAcc) {
		return bytesToHexList(bytes)
				.stream()
				.reduce(reduceAcc)
				.orElse("");
	}

	public static List<String> bytesToHexList(byte[] bytes) {
		List<String> hexList = new ArrayList<>();

		if (bytes == null) {
			return hexList;
		}

		for (byte aByte : bytes) {
			hexList.add(byteToHex(aByte));
		}

		return hexList;
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

	public static int getBitValue(byte target, int index) {
		return (target >> index) & 1;
	}

}
