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

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.utils.ConfigurationUtils;
import org.gradle.api.attributes.AttributeContainer;

import java.util.Objects;

import static dev.nokee.runtime.nativebase.BuildType.BUILD_TYPE_COORDINATE_AXIS;

final class DefaultTargetBuildType implements TargetBuildType, Coordinate<BuildType>, ConfigurationUtils.ConfigurationAttributesProvider {
	private final BuildType buildType;

	DefaultTargetBuildType(BuildType buildType) {
		this.buildType = buildType;
	}

	@Override
	public void forConsuming(AttributeContainer attributes) {
		attributes.attribute(BuildType.BUILD_TYPE_ATTRIBUTE, buildType);
	}

	@Override
	public void forResolving(AttributeContainer attributes) {
		attributes.attribute(BuildType.BUILD_TYPE_ATTRIBUTE, buildType);
	}

	@Override
	public BuildType getValue() {
		return buildType;
	}

	@Override
	public CoordinateAxis<BuildType> getAxis() {
		return BUILD_TYPE_COORDINATE_AXIS;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof Coordinate) {
			return Objects.equals(getAxis(), ((Coordinate<?>) other).getAxis())
				&& Objects.equals(getValue(), ((Coordinate<?>) other).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getAxis(), getValue());
	}

	@Override
	public String toString() {
		return buildType.getName();
	}
}
