package breakout;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;

public class Functions {

	public static float percentage(int a, int b) {
		return Math.abs(a / (float) b);
	}
	
	public static float percentage(float a, float b) {
		return Math.abs(a / b);
	}

	public static int percentageScaled(int scale, int a, int b) {
		return (int) (scale * percentage(a, b));
	}

	public static float negateByChance(float value) {
		if (MathUtils.randomBoolean()) {
			return -value;
		}

		return value;
	}

	public static int toIntBitsRGBA(int r, int g, int b, int a) {
		return (r << 24) | (g << 16) | (b << 8) | a;
	}

	public static <T> T randomElement(T... ts) {
		return ts[MathUtils.random(ts.length - 1)];
	}

	public static <T> T randomElement(List<T> ts) {
		return ts.get(MathUtils.random(ts.size() - 1));
	}

}
