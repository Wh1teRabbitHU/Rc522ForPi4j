package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.model.communication.CommunicationResult;

public interface RC522Adapter {

	void init(int speed, int resetPin, int spiChannel);

	void reset();

	CommunicationResult selectCard();

	CommunicationResult authCard(byte authMode, byte blockAddress, byte[] key, byte[] uid);

	CommunicationResult read(byte blockAddress);

	CommunicationResult write(byte blockAddress, byte[] data);

}
