package eu._4fh.abstract_bnet_api.util;

import java.util.Objects;

public class Pair<T1, T2> {
	public final T1 value1;
	public final T2 value2;

	public Pair(final T1 value1, final T2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value1, value2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		return Objects.equals(value1, other.value1) && Objects.equals(value2, other.value2);
	}

	@Override
	public String toString() {
		return "Pair [value1=" + value1 + ", value2=" + value2 + "]";
	}
}
