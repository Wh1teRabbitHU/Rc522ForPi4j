package hu.whiterabbit.rc522forpi4j.model.card;

import static hu.whiterabbit.rc522forpi4j.util.CardUtil.blockTypeToString;
import static hu.whiterabbit.rc522forpi4j.util.CardUtil.getBlockAccessMode;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.*;
import static java.lang.System.arraycopy;

public class SectorTrailerBlock implements Block {

	public static final int SECTOR_TRAILER_BLOCK_INDEX = 3;

	private byte[] keyA;

	private byte[] keyB;

	private byte[] accessBytes;

	private byte dataByte;

	private BlockReadStatus readStatus;

	private BlockAccessMode accessMode;

	public SectorTrailerBlock(byte[] data, BlockReadStatus readStatus) {
		if (data == null || readStatus != BlockReadStatus.SUCCESS) {
			data = new byte[BYTE_COUNT];
		}

		this.readStatus = readStatus;
		this.keyA = getByteRange(data, 0, 6);
		this.accessBytes = getByteRange(data, 6, 3);
		this.dataByte = data[9];
		this.keyB = getByteRange(data, 10, 6);
	}

	@Override
	public BlockReadStatus getReadStatus() {
		return readStatus;
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
	public BlockAccessMode getAccessMode() {
		return accessMode;
	}

	public void setAccessMode(BlockAccessMode accessMode) {
		this.accessMode = accessMode;
	}

	public void updateAccessMode(SectorTrailerBlock sectorTrailerBlock) {
		this.accessMode = getBlockAccessMode(this, sectorTrailerBlock.getAccessBytes());
	}

	@Override
	public int getIndex() {
		return SECTOR_TRAILER_BLOCK_INDEX;
	}

	@Override
	public byte[] getData() {
		byte[] data = new byte[BYTE_COUNT];

		arraycopy(keyA, 0, data, 0, keyA.length);
		arraycopy(accessBytes, 0, data, 6, accessBytes.length);
		data[9] = dataByte;
		arraycopy(keyB, 0, data, 10, keyB.length);

		return data;
	}

	@Override
	public BlockType getBlockType() {
		return BlockType.SECTOR_TRAILER;
	}

	@Override
	public String toString() {
		return "\tBlock (3) " + blockTypeToString(getBlockType()) +
				"\tkeyA=" + bytesToHex(keyA) +
				", keyB=" + bytesToHex(keyB) +
				", accessBytes=" + bytesToHex(accessBytes) +
				", dataByte=" + byteToHex(dataByte) +
				"\t" + getAccessMode().toString() +
				"\t Read result: " + getReadStatus();
	}

}
