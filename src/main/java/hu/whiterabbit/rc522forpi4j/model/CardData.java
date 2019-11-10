package hu.whiterabbit.rc522forpi4j.model;

import java.util.ArrayList;
import java.util.List;

import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class CardData {

	private final byte[] tagId;

	private final List<DataBlock> dataBlockList = new ArrayList<>();

	public CardData(byte[] tagId) {
		this.tagId = tagId;
	}

	public byte[] getTagId() {
		return tagId;
	}

	public String getTagIdAsString() {
		return tagId == null ? null : bytesToHex(tagId);
	}

	public List<DataBlock> getDataBlockList() {
		return dataBlockList;
	}

	public void addDataBlock(DataBlock dataBlock) {
		dataBlockList.add(dataBlock);
	}

	public void addDataBlock(int blockAddress, byte[] byteData) {
		dataBlockList.add(new DataBlock(blockAddress, byteData));
	}

	@Override
	public String toString() {
		return "CardData {\n" +
				"\ttagId=" + getTagIdAsString() +
				",\n\tdataBlockList=[\n\t\t" + dataBlockList
				.stream()
				.map(DataBlock::toString)
				.reduce((a, b) -> a + ",\n\t\t" + b)
				.orElse("-") +
				"\n\t]\n}";
	}
}
