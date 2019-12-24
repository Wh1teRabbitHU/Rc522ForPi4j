package hu.whiterabbit.rc522forpi4j.rc522;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RC522ClientImplTest {

	@Mock
	private RC522Adapter rc522Adapter;

	@InjectMocks
	private RC522ClientImpl rc522Client;

	@Test
	public void testInit() {
		rc522Client.init();

		verify(rc522Adapter, times(1)).init(anyInt(), anyInt(), anyInt());
	}
}