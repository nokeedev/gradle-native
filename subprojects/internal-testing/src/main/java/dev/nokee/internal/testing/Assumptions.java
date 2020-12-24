package dev.nokee.internal.testing;

public final class Assumptions {
	private Assumptions() {}

	public static <T> T skipCurrentTestExecution(String because) {
		org.junit.jupiter.api.Assumptions.assumeTrue(false, because);
		throw new UnsupportedOperationException(because);
	}
}
