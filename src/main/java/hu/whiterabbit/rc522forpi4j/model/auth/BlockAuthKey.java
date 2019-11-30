package hu.whiterabbit.rc522forpi4j.model.auth;

import hu.whiterabbit.rc522forpi4j.model.card.SectorTrailerBlock;

import static hu.whiterabbit.rc522forpi4j.model.card.SectorTrailerBlock.SECTOR_TRAILER_BLOCK_INDEX;

public class BlockAuthKey {

	private static final byte[] FACTORY_DEFAULT_KEY = new byte[]{
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
	};

	private final int blockIndex;

	private AuthKeyType keyType;

	private byte[] key;

	public BlockAuthKey(int blockIndex) {
		this.blockIndex = blockIndex;
	}

	public int getBlockIndex() {
		return blockIndex;
	}

	public AuthKeyType getKeyType() {
		return keyType;
	}

	public void setKeyType(AuthKeyType keyType) {
		this.keyType = keyType;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public static BlockAuthKey getFactoryDefaultSectorKey() {
		BlockAuthKey authKey = new BlockAuthKey(SECTOR_TRAILER_BLOCK_INDEX);

		authKey.setKeyType(AuthKeyType.AUTH_A);
		authKey.setKey(FACTORY_DEFAULT_KEY);

		return authKey;
	}

	public static BlockAuthKey getFactoryDefaultKey(int blockIndex) {
		BlockAuthKey authKey = new BlockAuthKey(blockIndex);

		authKey.setKeyType(AuthKeyType.AUTH_A);
		authKey.setKey(FACTORY_DEFAULT_KEY);

		return authKey;
	}
}
