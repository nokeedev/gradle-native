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

import dev.nokee.runtime.core.CoordinateAxis;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

import java.util.Objects;

/**
 * Represent a build-type for a native binary.
 * Common build types are 'debug' and 'release', but others may be defined.
 *
 * @since 0.5
 */
public abstract class BuildType implements Named {
	/**
	 * The build type attribute for dependency resolution.
	 */
	public static final Attribute<BuildType> BUILD_TYPE_ATTRIBUTE = Attribute.of("dev.nokee.buildType", BuildType.class);

	/**
	 * The build type coordinate axis for variant calculation.
	 */
	public static final CoordinateAxis<BuildType> BUILD_TYPE_COORDINATE_AXIS = CoordinateAxis.of(BuildType.class, "build-type");

	/**
	 * Creates an build type instance of the specified name.
	 *
	 * @param name the name of the build type, must not be null
	 * @return a build type instance representing the specified name, never null
	 */
	public static BuildType named(String name) {
		return new DefaultBuildType(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof BuildType)) {
			return false;
		}

		BuildType other = (BuildType) obj;
		return Objects.equals(getName(), other.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
