package hu.whiterabbit.rc522forpi4j.model.card;

import static hu.whiterabbit.rc522forpi4j.util.CardUtil.blockTypeToString;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.*;

public class SectorTrailerBlock {

	private byte[] keyA;

	private byte[] keyB;

	private byte[] accessBytes;

	private byte dataByte;

	public SectorTrailerBlock(byte[] data) {
		this.keyA = getByteRange(data, 0, 6);
		this.keyB = getByteRange(data, 10, 6);
		this.accessBytes = getByteRange(data, 6, 3);
		this.dataByte = data[9];
	}

	public byte[] getKeyA() {
		return keyA;
	}

	public void setKeyA(byte[] keyA) {
		this.keyA = keyA;
	}

	public byte[] getKeyB() {
		return keyB;
	}

	public void setKeyB(byte[] keyB) {
		this.keyB = keyB;
	}

	public byte[] getAccessBytes() {
		return accessBytes;
	}

	public void setAccessBytes(byte[] accessBytes) {
		this.accessBytes = accessBytes;
	}

	public byte getDataByte() {
		return dataByte;
	}

	public void setDataByte(byte dataByte) {
		this.dataByte = dataByte;
	}

	@Override
	public String toString() {
		return "\tBlock (3) " + blockTypeToString(BlockType.SECTOR_TRAILER) +
				"\tkeyA=" + bytesToHex(keyA) +
				", keyB=" + bytesToHex(keyB) +
				", accessBytes=" + bytesToHex(accessBytes) +
				", dataByte=" + byteToHex(dataByte);
	}
}
