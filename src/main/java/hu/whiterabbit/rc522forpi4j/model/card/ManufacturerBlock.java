package hu.whiterabbit.rc522forpi4j.model.card;

import static hu.whiterabbit.rc522forpi4j.util.CardUtil.blockTypeToString;
import static hu.whiterabbit.rc522forpi4j.util.CardUtil.getBlockAccessMode;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.getByteRange;
import static java.lang.System.arraycopy;

public class ManufacturerBlock implements Block {

	public static final int MANUFACTURER_BLOCK_INDEX = 0;

	public static final int MANUFACTURER_SECTOR_INDEX = 0;

	private final byte[] uid;

	private final byte[] manufacturerData;

	private BlockReadStatus readStatus;

	private BlockAccessMode accessMode;

	public ManufacturerBlock(byte[] data, BlockReadStatus readStatus) {
		this.uid = getByteRange(data, 0, 7);
		this.manufacturerData = getByteRange(data, 7, 9);
		this.readStatus = readStatus;
	}

	public byte[] getUid() {
		return uid;
	}

	public byte[] getManufacturerData() {
		return manufacturerData;
	}

	@Override
	public BlockReadStatus getReadStatus() {
		return readStatus;
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
		return MANUFACTURER_BLOCK_INDEX;
	}

	@Override
	public byte[] getData() {
		byte[] data = new byte[BYTE_COUNT];

		arraycopy(uid, 0, data, 0, uid.length);
		arraycopy(manufacturerData, 0, data, 7, manufacturerData.length);

		return data;
	}

	@Override
	public BlockType getBlockType() {
		return BlockType.MANUFACTURER;
	}

	@Override
	public String toString() {
		return "\tBlock (0) " + blockTypeToString(getBlockType()) +
				"\tuid=" + bytesToHex(uid) +
				", manufacturerData=" + bytesToHex(manufacturerData) +
				"\t\t\t" + getAccessMode().toString() +
				"\t Read result: " + getReadStatus();
	}
}
