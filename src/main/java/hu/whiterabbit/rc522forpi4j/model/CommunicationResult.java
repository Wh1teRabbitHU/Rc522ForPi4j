package hu.whiterabbit.rc522forpi4j.model;

public class CommunicationResult {

	private byte[] data = new byte[16];

	private int length = 0;

	private int bits = 0;

	private CommunicationStatus status;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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
