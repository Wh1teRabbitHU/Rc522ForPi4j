package hu.whiterabbit.rc522forpi4j.model.communication;

import java.util.Arrays;

import static hu.whiterabbit.rc522forpi4j.model.card.Block.BYTE_COUNT;

public class CommunicationResult {

	private byte[] data = new byte[BYTE_COUNT];

	private int length = 0;

	private int bits = 0;

	private CommunicationStatus status;

	public byte[] getData() {
		return data;
	}

	public byte[] getData(int length) {
		return Arrays.copyOf(data, length);
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte getDataByte(int index) {
		return data[index];
	}

	public void setDataByte(int index, byte value) {
		data[index] = value;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getBits() {
		return bits;
	}

	public void setBits(int bits) {
		this.bits = bits;
	}

	public CommunicationStatus getStatus() {
		return status;
	}

	public void setStatus(CommunicationStatus status) {
		this.status = status;
	}

	public boolean isSuccess() {
		return status == CommunicationStatus.SUCCESS;
	}
}
