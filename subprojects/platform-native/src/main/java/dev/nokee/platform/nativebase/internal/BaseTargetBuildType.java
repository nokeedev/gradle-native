package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.TargetBuildType;
import org.gradle.api.attributes.Attribute;

public class BaseTargetBuildType implements TargetBuildType, Coordinate<TargetBuildType> {
	public static final Attribute<String> BUILD_TYPE_ATTRIBUTE = Attribute.of("dev.nokee.buildType", String.class);
	public static final CoordinateAxis<TargetBuildType> BUILD_TYPE_COORDINATE_AXIS = CoordinateAxis.of(TargetBuildType.class);

	@Override
	public CoordinateAxis<TargetBuildType> getAxis() {
		return BUILD_TYPE_COORDINATE_AXIS;
	}
}
