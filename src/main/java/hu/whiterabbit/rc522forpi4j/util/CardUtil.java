package hu.whiterabbit.rc522forpi4j.util;

import hu.whiterabbit.rc522forpi4j.model.card.DataBlock;
import hu.whiterabbit.rc522forpi4j.model.card.BlockAccessMode;
import hu.whiterabbit.rc522forpi4j.model.card.BlockType;

import static hu.whiterabbit.rc522forpi4j.util.AccessModeBit.*;

public class CardUtil {

	private static final AccessModeBit[][] ACCESS_MODE_BIT_INDEX_MATRIX = {
			{C3_2_N, C2_2_N, C1_2_N, C0_2_N, C3_1_N, C2_1_N, C1_1_N, C0_1_N},
			{C3_1, C2_1, C1_1, C0_1, C3_3_N, C2_3_N, C1_3_N, C0_3_N},
			{C3_3, C2_3, C1_3, C0_3, C3_2, C2_2, C1_2, C0_2}
	};

	private CardUtil() {
	}

	public static String blockTypeToString(BlockType blockType) {
		int blockTypeLength = blockType.toString().length();
		int maxBlockTypeLength = BlockType.SECTOR_TRAILER.toString().length();
		int extraSpaceNeeded = maxBlockTypeLength - blockTypeLength;

		return "[" + blockType + "]" + new String(new char[extraSpaceNeeded]).replace("\0", " ");
	}

	public static BlockAccessMode getBlockAccessMode(DataBlock dataBlock, byte[] accessBytes) {
		BlockAccessMode blockAccessMode = new BlockAccessMode();

		int C1 = getAccessBit(accessBytes, dataBlock.getIndex(), 1, false);
		int C2 = getAccessBit(accessBytes, dataBlock.getIndex(), 2, false);
		int C3 = getAccessBit(accessBytes, dataBlock.getIndex(), 3, false);

		blockAccessMode.setC1(C1);
		blockAccessMode.setC2(C2);
		blockAccessMode.setC3(C3);

		return blockAccessMode;
	}

	private static int getAccessBit(byte[] accessBytes, int blockIndex, int bitIndex, boolean isNegated) {
		for (int i = 0; i < ACCESS_MODE_BIT_INDEX_MATRIX.length; i++) {
			int accessBit = getAccessBit(ACCESS_MODE_BIT_INDEX_MATRIX[i], accessBytes[i], blockIndex, bitIndex, isNegated);

			if (accessBit != -1) {
				return accessBit;
			}
		}

		return -1;
	}

	private static int getAccessBit(AccessModeBit[] accessModeBits, byte accessByte, int blockIndex, int bitIndex, boolean isNegated) {
		for (int i = 0; i < accessModeBits.length; i++) {
			AccessModeBit bit = accessModeBits[i];

			if (bit.blockIndex == blockIndex && bit.bitIndex == bitIndex && bit.isNegated == isNegated) {
				return DataUtil.getBitValue(accessByte, 7 - i);
			}
		}

		return -1;
	}

}
