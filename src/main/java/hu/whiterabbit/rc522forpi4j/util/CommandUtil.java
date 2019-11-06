package hu.whiterabbit.rc522forpi4j.util;

@SuppressWarnings("unused")
public class CommandUtil {

    public static final byte PCD_IDLE = 0x00;

    public static final byte PCD_AUTHENT = 0x0E;

    public static final byte PCD_RECEIVE = 0x08;

    public static final byte PCD_TRANSMIT = 0x04;

    public static final byte PCD_TRANSCEIVE = 0x0C;

    public static final byte PCD_RESETPHASE = 0x0F;

    public static final byte PCD_CALCCRC = 0x03;

    public static final byte PICC_REQIDL = 0x26;

    public static final byte PICC_REQALL = 0x52;

    public static final byte PICC_ANTICOLL = (byte) 0x93;

    public static final byte PICC_SElECTTAG = (byte) 0x93;

    public static final byte PICC_AUTHENT1A = 0x60;

    public static final byte PICC_AUTHENT1B = 0x61;

    public static final byte PICC_READ = 0x30;

    public static final byte PICC_WRITE = (byte) 0xA0;

    public static final byte PICC_DECREMENT = (byte) 0xC0;

    public static final byte PICC_INCREMENT = (byte) 0xC1;

    public static final byte PICC_RESTORE = (byte) 0xC2;

    public static final byte PICC_TRANSFER = (byte) 0xB0;

    public static final byte PICC_HALT = 0x50;

    public static final int MI_OK = 0;

    public static final int MI_NOTAGERR = 1;

    public static final int MI_ERR = 2;

    public static final byte RESERVED_00 = 0x00;

    public static final byte COMMAND_REG = 0x01;

    public static final byte COMM_I_EN_REG = 0x02;

    public static final byte DIVL_EN_REG = 0x03;

    public static final byte COMM_IRQ_REG = 0x04;

    public static final byte DIV_IRQ_REG = 0x05;

    public static final byte ERROR_REG = 0x06;

    public static final byte STATUS_1_REG = 0x07;

    public static final byte STATUS_2_REG = 0x08;

    public static final byte FIFO_DATA_REG = 0x09;

    public static final byte FIFO_LEVEL_REG = 0x0A;

    public static final byte WATER_LEVEL_REG = 0x0B;

    public static final byte CONTROL_REG = 0x0C;

    public static final byte BIT_FRAMING_REG = 0x0D;

    public static final byte COLL_REG = 0x0E;

    public static final byte RESERVED_01 = 0x0F;

    public static final byte RESERVED_10 = 0x10;

    public static final byte MODE_REG = 0x11;

    public static final byte TX_MODE_REG = 0x12;

    public static final byte RX_MODE_REG = 0x13;

    public static final byte TX_CONTROL_REG = 0x14;

    public static final byte TX_AUTO_REG = 0x15;

    public static final byte TX_SEL_REG = 0x16;

    public static final byte RX_SEL_REG = 0x17;

    public static final byte RX_THRESHOLD_REG = 0x18;

    public static final byte DEMOD_REG = 0x19;

    public static final byte RESERVED_11 = 0x1A;

    public static final byte RESERVED_12 = 0x1B;

    public static final byte MIFARE_REG = 0x1C;

    public static final byte RESERVED_13 = 0x1D;

    public static final byte RESERVED_14 = 0x1E;

    public static final byte SERIAL_SPEED_REG = 0x1F;

    public static final byte RESERVED_20 = 0x20;

    public static final byte CRC_RESULT_REG_M = 0x21;

    public static final byte CRC_RESULT_REG_L = 0x22;

    public static final byte RESERVED_21 = 0x23;

    public static final byte MOD_WIDTH_REG = 0x24;

    public static final byte RESERVED_22 = 0x25;

    public static final byte RF_CFG_REG = 0x26;

    public static final byte GS_N_REG = 0x27;

    public static final byte CW_GS_P_REG = 0x28;

    public static final byte MOD_GS_P_REG = 0x29;

    public static final byte T_MODE_REG = 0x2A;

    public static final byte T_PRESCALER_REG = 0x2B;

    public static final byte T_RELOAD_REG_H = 0x2C;

    public static final byte T_RELOAD_REG_L = 0x2D;

    public static final byte T_COUNTER_VALUE_REG_H = 0x2E;

    public static final byte T_COUNTER_VALUE_REG_L = 0x2F;

    public static final byte RESERVED_30 = 0x30;

    public static final byte TEST_SEL_1_REG = 0x31;

    public static final byte TEST_SEL_2_REG = 0x32;

    public static final byte TEST_PIN_EN_REG = 0x33;

    public static final byte TEST_PIN_VALUE_REG = 0x34;

    public static final byte TEST_BUS_REG = 0x35;

    public static final byte AUTO_TEST_REG = 0x36;

    public static final byte VERSION_REG = 0x37;

    public static final byte ANALOG_TEST_REG = 0x38;

    public static final byte TEST_DAC_1_REG = 0x39;

    public static final byte TEST_DAC_2_REG = 0x3A;

    public static final byte TEST_ADC_REG = 0x3B;

    public static final byte RESERVED_31 = 0x3C;

    public static final byte RESERVED_32 = 0x3D;

    public static final byte RESERVED_33 = 0x3E;

    public static final byte RESERVED_34 = 0x3F;

}
