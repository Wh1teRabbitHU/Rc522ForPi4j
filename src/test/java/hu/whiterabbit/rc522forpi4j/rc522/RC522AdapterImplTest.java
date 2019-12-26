package hu.whiterabbit.rc522forpi4j.rc522;

import hu.whiterabbit.rc522forpi4j.raspberry.RaspberryPiAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RC522AdapterImplTest {

	private static final int RESET_PIN = 22;

	private static final int SPEED = 500000;

	private static final int SPI_CHANNEL = 0;

	@Mock
	private RaspberryPiAdapter piAdapter;

	@InjectMocks
	private RC522AdapterImpl rc522Adapter;

	@Test
	public void testInit() {
		rc522Adapter.init(SPEED, RESET_PIN, SPI_CHANNEL);

		verify(piAdapter, times(1)).init(eq(SPI_CHANNEL), eq(SPEED), eq(RESET_PIN));
		verify(piAdapter, times(10)).wiringPiSPIDataRW(anyInt(), any());
	}

}
