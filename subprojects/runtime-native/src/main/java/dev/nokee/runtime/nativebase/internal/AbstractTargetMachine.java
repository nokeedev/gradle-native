/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.ConfigurationUtils;
import lombok.EqualsAndHashCode;
import org.gradle.api.attributes.AttributeContainer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS;

@EqualsAndHashCode
abstract class AbstractTargetMachine implements TargetMachine, Coordinate<TargetMachine>, CoordinateTuple, ConfigurationUtils.ConfigurationAttributesProvider {
	private final Coordinate<MachineArchitecture> architectureCoordinate;
	private final Coordinate<OperatingSystemFamily> operatingSystemCoordinate;

	AbstractTargetMachine(OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
		this.architectureCoordinate = Coordinate.of(ARCHITECTURE_COORDINATE_AXIS, architecture);
		this.operatingSystemCoordinate = Coordinate.of(OPERATING_SYSTEM_COORDINATE_AXIS, operatingSystemFamily);
	}

	@Override
	public OperatingSystemFamily getOperatingSystemFamily() {
		return operatingSystemCoordinate.getValue();
	}

	@Override
	public MachineArchitecture getArchitecture() {
		return architectureCoordinate.getValue();
	}

	@Override
	public Iterator<Coordinate<?>> iterator() {
		return Stream.<Coordinate<?>>of(operatingSystemCoordinate, architectureCoordinate).iterator();
	}

	@Override
	public CoordinateAxis<TargetMachine> getAxis() {
		return TARGET_MACHINE_COORDINATE_AXIS;
	}

	@Override
	public void forConsuming(AttributeContainer attributes) {
		attributes.attribute(OPERATING_SYSTEM_ATTRIBUTE, getOperatingSystemFamily());
		attributes.attribute(ARCHITECTURE_ATTRIBUTE, getArchitecture());
		attributes.attribute(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, new StandaloneOperatingSystemFamily(getOperatingSystemFamily().getName()));
		attributes.attribute(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE, new StandaloneMachineArchitecture(getArchitecture().getName()));
	}

	@Override
	public void forResolving(AttributeContainer attributes) {
		attributes.attribute(OPERATING_SYSTEM_ATTRIBUTE, getOperatingSystemFamily());
		attributes.attribute(ARCHITECTURE_ATTRIBUTE, getArchitecture());
		attributes.attribute(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, new StandaloneOperatingSystemFamily(getOperatingSystemFamily().getName()));
		attributes.attribute(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE, new StandaloneMachineArchitecture(getArchitecture().getName()));
	}

	private static final class StandaloneOperatingSystemFamily extends org.gradle.nativeplatform.OperatingSystemFamily implements Serializable {
		private final String name;

		private StandaloneOperatingSystemFamily(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (!(o instanceof org.gradle.nativeplatform.OperatingSystemFamily)) {
				return false;
			}
			org.gradle.nativeplatform.OperatingSystemFamily other = (org.gradle.nativeplatform.OperatingSystemFamily) o;
			return Objects.equals(getName(), other.getName());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getName());
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final class StandaloneMachineArchitecture extends org.gradle.nativeplatform.MachineArchitecture implements Serializable {
		private final String name;

		private StandaloneMachineArchitecture(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (!(o instanceof org.gradle.nativeplatform.MachineArchitecture)) {
				return false;
			}
			org.gradle.nativeplatform.MachineArchitecture other = (org.gradle.nativeplatform.MachineArchitecture) o;
			return Objects.equals(getName(), other.getName());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getName());
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
