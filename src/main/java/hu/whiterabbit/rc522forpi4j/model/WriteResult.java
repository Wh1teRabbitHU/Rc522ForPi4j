package hu.whiterabbit.rc522forpi4j.model;

public class WriteResult {

	private byte[] backData = new byte[16];

	private int backLen = 0;

	private int backBits;

	private CommunicationStatus status;

	public byte[] getBackData() {
		return backData;
	}

	public void setBackData(byte[] backData) {
		this.backData = backData;
	}

	public int getBackLen() {
		return backLen;
	}

	public void setBackLen(int backLen) {
		this.backLen = backLen;
	}

	public int getBackBits() {
		return backBits;
	}

	public void setBackBits(int backBits) {
		this.backBits = backBits;
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
