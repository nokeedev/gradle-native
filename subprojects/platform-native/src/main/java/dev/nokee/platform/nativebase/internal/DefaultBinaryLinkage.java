package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import org.gradle.api.Named;

public class DefaultBinaryLinkage implements Named, Dimension {
	public static final DimensionType<DefaultBinaryLinkage> DIMENSION_TYPE = new DefaultDimensionType(DefaultBinaryLinkage.class);
	public static final DefaultBinaryLinkage STATIC = new DefaultBinaryLinkage("static");
	public static final DefaultBinaryLinkage SHARED = new DefaultBinaryLinkage("shared");
	public static final DefaultBinaryLinkage EXECUTABLE = new DefaultBinaryLinkage("executable");
	public static final DefaultBinaryLinkage BUNDLE = new DefaultBinaryLinkage("bundle");
	private final String name;

	private DefaultBinaryLinkage(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DimensionType getType() {
		return DIMENSION_TYPE;
	}
}
