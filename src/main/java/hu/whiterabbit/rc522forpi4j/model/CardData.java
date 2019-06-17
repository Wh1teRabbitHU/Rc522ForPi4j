package hu.whiterabbit.rc522forpi4j.model;

import java.util.ArrayList;
import java.util.List;

import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class CardData {

	private final byte[] tagData;

	private final List<DataBlock> dataBlockList = new ArrayList<>();

	public CardData(byte[] tagData) {
		this.tagData = tagData;
	}

	public byte[] getTagData() {
		return tagData;
	}

	public String getTag() {
		return tagData == null ? null : bytesToHex(tagData);
	}

	public List<DataBlock> getDataBlockList() {
		return dataBlockList;
	}

	public void addDataBlock(DataBlock dataBlock) {
		dataBlockList.add(dataBlock);
	}
}
