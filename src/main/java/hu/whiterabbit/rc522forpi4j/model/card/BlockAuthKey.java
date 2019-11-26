package hu.whiterabbit.rc522forpi4j.model.card;

public class BlockAuthKey {

	public static final byte[] FACTORY_DEFAULT_KEY = new byte[]{
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
	};

	private int blockIndex;

	private AuthKeyType keyType;

	private byte[] key;

	public int getBlockIndex() {
		return blockIndex;
	}

	public void setBlockIndex(int blockIndex) {
		this.blockIndex = blockIndex;
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
}
