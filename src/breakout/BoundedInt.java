package breakout;


public class BoundedInt {
	private int value;
	private int min;
	private int max;

	public BoundedInt(int value, int min, int max) {
		if (value < min) {
			throw new IllegalArgumentException(String.format("Value can not be smaller than minimum: %d < %d", value, min));
		}
		if (value > max) {
			throw new IllegalArgumentException(String.format("Value can not be larger than minimum: %d > %d", value, max));
		}

		this.value = value;
		this.min = min;
		this.max = max;
	}

	public void subtract(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException(String.format("Amount can not be smaller than 0: %d < 0", amount));
		}

		value -= Math.abs(amount);
		if (value < min) {
			value = min;
		}
	}

	public void add(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException(String.format("Amount can not be smaller than 0: %d < 0", amount));
		}

		value += amount;
		if (value > max) {
			value = max;
		}
	}

	public void set(int value) {
		this.value = value;

		if (value > max) {
			value = max;
		}
		if (value < min) {
			value = min;
		}
	}

	public void setMin() {
		value = min;
	}

	public void setMax() {
		value = max;
	}

	public boolean isMin() {
		return value == min;
	}

	public boolean isMax() {
		return value == max;
	}

	public int value() {
		return value;
	}

	public int min() {
		return min;
	}

	public int max() {
		return max;
	}

	@Override
	public String toString() {
		return "(" + value + ", " + min + ", " + max + ")";
	}

}