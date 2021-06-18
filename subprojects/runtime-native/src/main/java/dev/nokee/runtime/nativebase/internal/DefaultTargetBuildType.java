package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.ProvideAttributes;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import org.gradle.api.attributes.AttributeContainer;

import static dev.nokee.runtime.nativebase.BuildType.BUILD_TYPE_COORDINATE_AXIS;

// TODO: Maybe coordinate should be of the BuildType instead
final class DefaultTargetBuildType implements TargetBuildType, ProvideAttributes, Coordinate<BuildType> {
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
	public String toString() {
		return buildType.getName();
	}
}
