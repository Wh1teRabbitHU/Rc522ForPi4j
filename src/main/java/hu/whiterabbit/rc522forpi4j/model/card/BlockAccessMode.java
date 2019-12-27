package hu.whiterabbit.rc522forpi4j.model.card;

public class BlockAccessMode {

	private boolean c1;

	private boolean c2;

	private boolean c3;

	public boolean isC1() {
		return c1;
	}

	public void setC1(boolean c1) {
		this.c1 = c1;
	}

	public void setC1(int c1) {
		this.c1 = c1 == 1;
	}

	public boolean isC2() {
		return c2;
	}

	public void setC2(boolean c2) {
		this.c2 = c2;
	}

	public void setC2(int c2) {
		this.c2 = c2 == 1;
	}

	public boolean isC3() {
		return c3;
	}

	public void setC3(boolean c3) {
		this.c3 = c3;
	}

	public void setC3(int c3) {
		this.c3 = c3 == 1;
	}

	@Override
	public String toString() {
		return "accessMode { " +
				"c1=" + c1 +
				", c2=" + c2 +
				", c3=" + c3 +
				" }";
	}
}
