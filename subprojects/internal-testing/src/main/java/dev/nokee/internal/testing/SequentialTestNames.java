package dev.nokee.internal.testing;

public final class SequentialTestNames {
	private int count = 0;

	public String next() {
		return String.valueOf("n" + count++);
	}
}
