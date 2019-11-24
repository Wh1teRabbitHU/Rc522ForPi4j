package hu.whiterabbit.rc522forpi4j.model.card;

import static hu.whiterabbit.rc522forpi4j.util.CardUtil.blockTypeToString;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.getByteRange;

public class ManufacturerBlock {

	private final byte[] uid;

	private final byte[] manufacturerData;

	public ManufacturerBlock(byte[] data) {
		this.uid = getByteRange(data, 0, 7);
		this.manufacturerData = getByteRange(data, 7, 9);
	}

	public byte[] getUid() {
		return uid;
	}

	public byte[] getManufacturerData() {
		return manufacturerData;
	}

	@Override
	public String toString() {
		return "\tBlock (0) " + blockTypeToString(BlockType.MANUFACTURER) +
				"\tuid=" + bytesToHex(uid) +
				", manufacturerData=" + bytesToHex(manufacturerData);
	}
}
