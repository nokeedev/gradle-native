package dev.nokee.runtime.nativebase;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public final class MachineArchitectureUnderTest {
	private final String name;
	private final String canonicalName;

	private MachineArchitectureUnderTest(String name, String canonicalName) {
		this.name = requireNonNull(name);
		this.canonicalName = requireNonNull(canonicalName);
	}

	/**
	 * Creates a new machine architecture under test with the specified name as both name and canonical name.
	 * @param canonicalName  the architecture canonical name, must not be null
	 * @return a new machine architecture under test, never null
	 */
	public static MachineArchitectureUnderTest canonical(String canonicalName) {
		return new MachineArchitectureUnderTest(canonicalName, canonicalName);
	}

	public String getName() {
		return name;
	}

	public String getCanonicalName() {
		return canonicalName;
	}

	/**
	 * Creates a new machine architecture with the specified name, keeping the current canonical name.
	 *
	 * @param name  the new machine architecture name, must not be null
	 * @return a new machine architecture under test, never null
	 */
	public MachineArchitectureUnderTest withName(String name) {
		return new MachineArchitectureUnderTest(name, canonicalName);
	}


	public void assertCanonicalName(MachineArchitecture subject) {
		assertThat(subject.getCanonicalName(), equalTo(canonicalName));
	}

	@Override
	public String toString() {
		return name;
	}
}
