package hu.whiterabbit.rc522forpi4j.rc522;

@SuppressWarnings("unused")
class RC522CommandTable {

	static final byte PCD_IDLE = 0x00;

	static final byte PCD_AUTHENT = 0x0E;

	static final byte PCD_RECEIVE = 0x08;

	static final byte PCD_TRANSMIT = 0x04;

	static final byte PCD_TRANSCEIVE = 0x0C;

	static final byte PCD_RESETPHASE = 0x0F;

	static final byte PCD_CALCCRC = 0x03;

	static final byte PICC_REQIDL = 0x26;

	static final byte PICC_REQALL = 0x52;

	static final byte PICC_ANTICOLL = (byte) 0x93;

	static final byte PICC_SElECTTAG = (byte) 0x93;

	static final byte PICC_AUTHENT1A = 0x60;

	static final byte PICC_AUTHENT1B = 0x61;

	static final byte PICC_READ = 0x30;

	static final byte PICC_WRITE = (byte) 0xA0;

	static final byte PICC_DECREMENT = (byte) 0xC0;

	static final byte PICC_INCREMENT = (byte) 0xC1;

	static final byte PICC_RESTORE = (byte) 0xC2;

	static final byte PICC_TRANSFER = (byte) 0xB0;

	static final byte PICC_HALT = 0x50;

	static final int MI_OK = 0;

	static final int MI_NOTAGERR = 1;

	static final int MI_ERR = 2;

	static final byte RESERVED_00 = 0x00;

	static final byte COMMAND_REG = 0x01;

	static final byte COMM_I_EN_REG = 0x02;

	static final byte DIVL_EN_REG = 0x03;

	static final byte COMM_IRQ_REG = 0x04;

	static final byte DIV_IRQ_REG = 0x05;

	static final byte ERROR_REG = 0x06;

	static final byte STATUS_1_REG = 0x07;

	static final byte STATUS_2_REG = 0x08;

	static final byte FIFO_DATA_REG = 0x09;

	static final byte FIFO_LEVEL_REG = 0x0A;

	static final byte WATER_LEVEL_REG = 0x0B;

	static final byte CONTROL_REG = 0x0C;

	static final byte BIT_FRAMING_REG = 0x0D;

	static final byte COLL_REG = 0x0E;

	static final byte RESERVED_01 = 0x0F;

	static final byte RESERVED_10 = 0x10;

	static final byte MODE_REG = 0x11;

	static final byte TX_MODE_REG = 0x12;

	static final byte RX_MODE_REG = 0x13;

	static final byte TX_CONTROL_REG = 0x14;

	static final byte TX_AUTO_REG = 0x15;

	static final byte TX_SEL_REG = 0x16;

	static final byte RX_SEL_REG = 0x17;

	static final byte RX_THRESHOLD_REG = 0x18;

	static final byte DEMOD_REG = 0x19;

	static final byte RESERVED_11 = 0x1A;

	static final byte RESERVED_12 = 0x1B;

	static final byte MIFARE_REG = 0x1C;

	static final byte RESERVED_13 = 0x1D;

	static final byte RESERVED_14 = 0x1E;

	static final byte SERIAL_SPEED_REG = 0x1F;

	static final byte RESERVED_20 = 0x20;

	static final byte CRC_RESULT_REG_M = 0x21;

	static final byte CRC_RESULT_REG_L = 0x22;

	static final byte RESERVED_21 = 0x23;

	static final byte MOD_WIDTH_REG = 0x24;

	static final byte RESERVED_22 = 0x25;

	static final byte RF_CFG_REG = 0x26;

	static final byte GS_N_REG = 0x27;

	static final byte CW_GS_P_REG = 0x28;

	static final byte MOD_GS_P_REG = 0x29;

	static final byte T_MODE_REG = 0x2A;

	static final byte T_PRESCALER_REG = 0x2B;

	static final byte T_RELOAD_REG_H = 0x2C;

	static final byte T_RELOAD_REG_L = 0x2D;

	static final byte T_COUNTER_VALUE_REG_H = 0x2E;

	static final byte T_COUNTER_VALUE_REG_L = 0x2F;

	static final byte RESERVED_30 = 0x30;

	static final byte TEST_SEL_1_REG = 0x31;

	static final byte TEST_SEL_2_REG = 0x32;

	static final byte TEST_PIN_EN_REG = 0x33;

	static final byte TEST_PIN_VALUE_REG = 0x34;

	static final byte TEST_BUS_REG = 0x35;

	static final byte AUTO_TEST_REG = 0x36;

	static final byte VERSION_REG = 0x37;

	static final byte ANALOG_TEST_REG = 0x38;

	static final byte TEST_DAC_1_REG = 0x39;

	static final byte TEST_DAC_2_REG = 0x3A;

	static final byte TEST_ADC_REG = 0x3B;

	static final byte RESERVED_31 = 0x3C;

	static final byte RESERVED_32 = 0x3D;

	static final byte RESERVED_33 = 0x3E;

	static final byte RESERVED_34 = 0x3F;

}
