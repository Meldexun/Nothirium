package meldexun.nothirium.util.math;

public class MathUtil {

	public static double clamp(double x, double min, double max) {
		return x <= min ? min : (x >= max ? max : x);
	}

	public static int floor(double x) {
		int i = (int) x;
		return x < i ? i - 1 : i;
	}

	public static int floorDiv(int x, int y) {
		int i = x / y;
		return x < 0 != y < 0 && x != y * i ? i - 1 : i;
	}

	public static int floorMod(int x, int y) {
		return x - floorDiv(x, y) * y;
	}

	public static int ceil(double x) {
		int i = (int) x;
		return x > i ? i + 1 : i;
	}

	public static int ceilDiv(int x, int y) {
		int i = x / y;
		return x < 0 == y < 0 && x != y * i ? i + 1 : i;
	}

	public static double square(double d) {
		return d * d;
	}

}
