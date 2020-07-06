package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import lombok.Value;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

@Value
public class DefaultBinaryLinkage implements TargetLinkage, Named, Dimension {
	public static final Attribute<String> LINKAGE_ATTRIBUTE = Attribute.of("dev.nokee.linkage", String.class);
	public static final DimensionType<DefaultBinaryLinkage> DIMENSION_TYPE = new DefaultDimensionType(DefaultBinaryLinkage.class);
	public static final DefaultBinaryLinkage STATIC = new DefaultBinaryLinkage("static");
	public static final DefaultBinaryLinkage SHARED = new DefaultBinaryLinkage("shared");
	public static final DefaultBinaryLinkage EXECUTABLE = new DefaultBinaryLinkage("executable");
	public static final DefaultBinaryLinkage BUNDLE = new DefaultBinaryLinkage("bundle");
	String name;

	@Override
	public DimensionType getType() {
		return DIMENSION_TYPE;
	}
}
