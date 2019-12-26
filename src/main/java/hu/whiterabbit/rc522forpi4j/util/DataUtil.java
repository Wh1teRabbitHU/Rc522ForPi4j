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

	/**
	 * It will convert the input byte value into a hexadecimal value
	 *
	 * @param byteValue Source byte value
	 * @return Converted hexadecimal string
	 */
	public static String byteToHex(byte byteValue) {
		int v = byteValue & 0xFF;
		return HEX_ARRAY[v >>> 4] + "" + HEX_ARRAY[v & 0x0F];
	}

	/**
	 * It will convert the input byte values into a hexadecimal value and concatenate them
	 *
	 * @param bytes Source byte array
	 * @return Converted hexadecimal string
	 */
	public static String bytesToHex(byte[] bytes) {
		return bytesToHex(bytes, (a, b) -> a + b);
	}

	/**
	 * It will convert the input byte values into a hexadecimal value and concatenate with the given reducer function
	 *
	 * @param bytes Source byte array
	 * @return Converted hexadecimal string
	 */
	public static String bytesToHex(byte[] bytes, BinaryOperator<String> reduceAcc) {
		return bytesToHexList(bytes)
				.stream()
				.reduce(reduceAcc)
				.orElse("");
	}

	/**
	 * Converts the communication status code into an enum
	 *
	 * @param statusCode The status code of the communication's result
	 * @return The evaluated CommunicationStatus enum instance
	 */
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

	/**
	 * Returns the bit value at a certain index from a byte value
	 *
	 * @param target Target byte value
	 * @param index  Index of the bit
	 * @return The bit value at the given index
	 */
	public static int getBitValue(byte target, int index) {
		return (target >> index) & 1;
	}

	/**
	 * Get a sub array of a byte array
	 *
	 * @param bytes         Source byte array
	 * @param startingIndex Sub array's starting index
	 * @param number        Length of the sub array
	 * @return The sub array instance
	 */
	public static byte[] getByteRange(byte[] bytes, int startingIndex, int number) {
		byte[] result = new byte[number];

		for (int i = 0; i < number; i++) {
			if (bytes == null || bytes.length <= startingIndex + i) {
				result[i] = 0;
			} else {
				result[i] = bytes[startingIndex + i];
			}
		}

		return result;
	}

	private static List<String> bytesToHexList(byte[] bytes) {
		List<String> hexList = new ArrayList<>();

		if (bytes == null) {
			return hexList;
		}

		for (byte aByte : bytes) {
			hexList.add(byteToHex(aByte));
		}

		return hexList;
	}

}
