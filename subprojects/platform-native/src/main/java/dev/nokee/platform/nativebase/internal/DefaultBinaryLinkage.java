package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.TargetLinkage;
import lombok.Value;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

@Value
public class DefaultBinaryLinkage implements TargetLinkage, Named, Coordinate<TargetLinkage> {
	public static final Attribute<String> LINKAGE_ATTRIBUTE = Attribute.of("dev.nokee.linkage", String.class);
	public static final CoordinateAxis<TargetLinkage> BINARY_LINKAGE_COORDINATE_AXIS = CoordinateAxis.of(TargetLinkage.class);
	public static final DefaultBinaryLinkage STATIC = new DefaultBinaryLinkage("static");
	public static final DefaultBinaryLinkage SHARED = new DefaultBinaryLinkage("shared");
	public static final DefaultBinaryLinkage EXECUTABLE = new DefaultBinaryLinkage("executable");
	public static final DefaultBinaryLinkage BUNDLE = new DefaultBinaryLinkage("bundle");
	String name;

	public boolean isShared() {
		return equals(SHARED);
	}

	public boolean isStatic() {
		return equals(STATIC);
	}

	public boolean isExecutable() {
		return equals(EXECUTABLE);
	}

	@Override
	public CoordinateAxis<TargetLinkage> getAxis() {
		return BINARY_LINKAGE_COORDINATE_AXIS;
	}
}
