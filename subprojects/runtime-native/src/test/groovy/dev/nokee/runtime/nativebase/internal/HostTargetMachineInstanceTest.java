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
package dev.nokee.runtime.nativebase.internal;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.core.Coordinates;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class HostTargetMachineInstanceTest {
	@Test
	void hasHostOperatingSystemFamily() {
		assertThat(TargetMachines.host().getOperatingSystemFamily(),
			equalTo(OperatingSystemFamily.forName(System.getProperty("os.name"))));
	}

	@Test
	void hasHostMachineArchitecture() {
		assertThat(TargetMachines.host().getArchitecture(),
			equalTo(MachineArchitecture.forName(System.getProperty("os.arch"))));
	}

	@Test
	void canRenameHostTargetMachine() {
		val subject = TargetMachines.host().named("foo");
		assertAll(
			() -> assertThat(subject.getOperatingSystemFamily(), equalTo(OperatingSystemFamily.forName(System.getProperty("os.name")))),
			() -> assertThat(subject.getArchitecture(), equalTo(MachineArchitecture.forName(System.getProperty("os.arch")))),
			() -> assertThat(subject, hasToString("foo"))
		);
	}

	@Test
	void checkToString() {
		assertThat(TargetMachines.host(), hasToString("host"));
	}

	@Test
	void hasCoordinateAxisAndValue() {
		val subject = TargetMachines.host();
		assertAll(
			() -> assertThat(subject.getAxis(), is(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)),
			() -> assertThat(subject.getValue(), is(subject))
		);
	}

	@Test
	void hasOperatingSystemAndArchitectureCoordinate() {
		val subject = TargetMachines.host();
		assertThat(subject, contains(Coordinates.of(subject.getOperatingSystemFamily()), Coordinates.of(subject.getArchitecture())));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(TargetMachines.host(), TargetMachines.host())
			.addEqualityGroup(TargetMachines.host().named("foo"))
			.testEquals();
	}
}
