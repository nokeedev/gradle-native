package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import org.gradle.api.attributes.Attribute;

public class BaseTargetBuildType implements TargetBuildType, Dimension {
	public static final Attribute<String> BUILD_TYPE_ATTRIBUTE = Attribute.of("dev.nokee.buildType", String.class);
	public static final DimensionType<BaseTargetBuildType> DIMENSION_TYPE = new DefaultDimensionType(BaseTargetBuildType.class);

	@Override
	public DimensionType getType() {
		return DIMENSION_TYPE;
	}
}
