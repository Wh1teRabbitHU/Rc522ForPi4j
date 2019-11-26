package hu.whiterabbit.rc522forpi4j.model.card;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hu.whiterabbit.rc522forpi4j.model.card.Card.MAX_CARD_SIZE;

public class CardAuthKey {

	private final List<SectorAuthKey> sectorAuthKeys = new ArrayList<>();

	public List<SectorAuthKey> getSectorAuthKeys() {
		return sectorAuthKeys;
	}

	public BlockAuthKey getBlockAuthKey(int sectorIndex, int blockIndex) {
		return sectorAuthKeys.stream()
				.filter(sectorAuthKey -> Objects.equals(sectorAuthKey.getSectorIndex(), sectorIndex))
				.flatMap(sectorAuthKey -> sectorAuthKey.getBlockAuthKeys().stream())
				.filter(blockAuthKey -> Objects.equals(blockAuthKey.getBlockIndex(), blockIndex))
				.findFirst()
				.orElse(null);
	}

	public void addSectorAuthKeys(SectorAuthKey sectorAuthKey) {
		sectorAuthKeys.add(sectorAuthKey);
	}

	public static CardAuthKey getFactoryDefaultKey() {
		CardAuthKey authKey = new CardAuthKey();

		for (int i = 0; i < MAX_CARD_SIZE; i++) {
			authKey.addSectorAuthKeys(SectorAuthKey.getFactoryDefaultKey(i));
		}

		return authKey;
	}
}
