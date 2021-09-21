/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
