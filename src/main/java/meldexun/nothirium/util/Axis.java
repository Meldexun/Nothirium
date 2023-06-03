package meldexun.nothirium.util;

public enum Axis {

	X,
	Y,
	Z;

	Direction positive;
	Direction negative;

	public Direction getPositive() {
		return positive;
	}

	public Direction getNegative() {
		return negative;
	}

}
