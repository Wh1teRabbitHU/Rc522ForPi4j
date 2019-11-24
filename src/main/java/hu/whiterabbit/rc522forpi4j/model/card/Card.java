package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.ArrayList;
import java.util.List;

import static hu.whiterabbit.rc522forpi4j.model.card.ManufacturerBlock.MANUFACTURER_BLOCK_INDEX;
import static hu.whiterabbit.rc522forpi4j.model.card.ManufacturerBlock.MANUFACTURER_SECTOR_INDEX;
import static hu.whiterabbit.rc522forpi4j.model.card.SectorTrailerBlock.SECTOR_TRAILER_BLOCK_INDEX;
import static hu.whiterabbit.rc522forpi4j.util.DataUtil.bytesToHex;

public class Card {

	public static final int MAX_CARD_SIZE = 16;

	private final byte[] tagId;

	private final List<Sector> sectorList = new ArrayList<>();

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
		if (sectorNumber < 0 || sectorNumber >= MAX_CARD_SIZE) {
			throw new RuntimeException("Given sector number is out of range! (" + sectorNumber + ")");
		}

		return sectorList
				.stream()
				.filter(sector -> sector.getIndex() == sectorNumber)
				.findFirst()
				.orElse(null);
	}

	public List<Sector> getSectorList() {
		return sectorList;
	}

	public void addSector(Sector sector) {
		Sector existingSector = getSector(sector.getIndex());

		if (existingSector != null) {
			throw new RuntimeException("Cannot add the given sector to this card. " +
					"Sector is already added with the following number: " + existingSector.getIndex());
		}

		sectorList.add(sector);
	}

	public void addBlock(int sectorIndex, int blockIndex, byte[] byteData) {
		Sector sector = getSector(sectorIndex);

		if (sector == null) {
			sector = new Sector(sectorIndex);

			addSector(sector);
		}

		if (blockIndex == SECTOR_TRAILER_BLOCK_INDEX) {
			sector.setSectorTrailerBlock(new SectorTrailerBlock(byteData));
		} else if (blockIndex == MANUFACTURER_BLOCK_INDEX && sectorIndex == MANUFACTURER_SECTOR_INDEX) {
			sector.setManufacturerBlock(new ManufacturerBlock(byteData));
		} else {
			sector.addBlock(new DataBlock(blockIndex, byteData));
		}
	}

	public void recalculateAccessModes() {
		for (int sectorIndex = 0; sectorIndex < MAX_CARD_SIZE; sectorIndex++) {
			Sector sector = getSector(sectorIndex);

			if (sector != null) {
				sector.recalculateAccessModes();
			}
		}
	}

	@Override
	public String toString() {
		return "\nCard [" + getTagIdAsString() +
				"]\n" + sectorList
				.stream()
				.map(Sector::toString)
				.reduce((sector, sector2) -> sector + "\n" + sector2)
				.orElse("");
	}
}
