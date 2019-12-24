package hu.whiterabbit.rc522forpi4j.raspberry;

public interface RaspberryPiAdapter {

	boolean init(int spiChannel, int speed, int resetPin);

	int wiringPiSPIDataRW(int channel, byte[] data);

}
