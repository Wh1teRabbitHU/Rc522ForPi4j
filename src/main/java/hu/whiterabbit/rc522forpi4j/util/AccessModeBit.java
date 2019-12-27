package hu.whiterabbit.rc522forpi4j.util;

enum AccessModeBit {

	C0_1(0, 1, false), C0_2(0, 2, false), C0_3(0, 3, false),
	C0_1_N(0, 1, true), C0_2_N(0, 2, true), C0_3_N(0, 3, true),
	C1_1(1, 1, false), C1_2(1, 2, false), C1_3(1, 3, false),
	C1_1_N(1, 1, true), C1_2_N(1, 2, true), C1_3_N(1, 3, true),
	C2_1(2, 1, false), C2_2(2, 2, false), C2_3(2, 3, false),
	C2_1_N(2, 1, true), C2_2_N(2, 2, true), C2_3_N(2, 3, true),
	C3_1(3, 1, false), C3_2(3, 2, false), C3_3(3, 3, false),
	C3_1_N(3, 1, true), C3_2_N(3, 2, true), C3_3_N(3, 3, true);

	public final int blockIndex;

	public final int bitIndex;

	public final Boolean isNegated;

	AccessModeBit(int blockIndex, int bitIndex, Boolean isNegated) {
		this.blockIndex = blockIndex;
		this.bitIndex = bitIndex;
		this.isNegated = isNegated;
	}
}
