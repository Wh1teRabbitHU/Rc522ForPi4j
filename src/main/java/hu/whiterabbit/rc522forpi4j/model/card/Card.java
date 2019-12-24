package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.ArrayList;
import java.util.List;

import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class Card {

	public static final int SECTOR_COUNT = 16;

	public static final int TAG_ID_SIZE = 5;

	private final byte[] tagId;

	private final List<Sector> sectors = new ArrayList<>();

	public Card(byte[] tagId) {
		this.tagId = tagId;
	}

	public byte[] getTagId() {
		return tagId;
	}

	public String getTagIdAsString() {
		return tagId == null ? null : bytesToHex(tagId);
	}

	public Sector getSector(int sectorNumber) {
		if (sectorNumber < 0 || sectorNumber >= SECTOR_COUNT) {
			throw new RuntimeException("Given sector number is out of range! (" + sectorNumber + ")");
		}

		return sectors
				.stream()
				.filter(sector -> sector.getIndex() == sectorNumber)
				.findFirst()
				.orElse(null);
	}

	public List<Sector> getSectors() {
		return sectors;
	}

	public void addSector(Sector sector) {
		Sector existingSector = getSector(sector.getIndex());

		if (existingSector != null) {
			throw new RuntimeException("Cannot add the given sector to this card. " +
					"Sector is already added with the following number: " + existingSector.getIndex());
		}

		sectors.add(sector);
	}

	public void addBlock(int sectorIndex, int blockIndex, byte[] byteData, BlockReadStatus readStatus) {
		Sector sector = getSector(sectorIndex);

		if (sector == null) {
			sector = new Sector(sectorIndex);

			addSector(sector);
		}

		sector.addBlock(blockIndex, byteData, readStatus);
	}

	public void recalculateAccessModes() {
		for (int sectorIndex = 0; sectorIndex < SECTOR_COUNT; sectorIndex++) {
			Sector sector = getSector(sectorIndex);

			if (sector != null) {
				sector.recalculateAccessModes();
			}
		}
	}

	@Override
	public String toString() {
		return "Card [" + getTagIdAsString() +
				"]\n" + sectors
				.stream()
				.map(Sector::toString)
				.reduce((sector, sector2) -> sector + "\n" + sector2)
				.orElse("");
	}
}
