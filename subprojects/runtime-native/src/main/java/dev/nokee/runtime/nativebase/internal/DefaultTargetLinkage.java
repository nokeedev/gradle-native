package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.utils.ConfigurationUtils;
import org.gradle.api.attributes.AttributeContainer;

import static dev.nokee.runtime.nativebase.BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS;

final class DefaultTargetLinkage implements TargetLinkage, Coordinate<BinaryLinkage>, ConfigurationUtils.AttributesProvider {
	private final BinaryLinkage binaryLinkage;

	DefaultTargetLinkage(BinaryLinkage binaryLinkage) {
		this.binaryLinkage = binaryLinkage;
	}

	@Override
	public void forConsuming(AttributeContainer attributes) {
		// no attributes required, use selection rule instead
	}

	@Override
	public void forResolving(AttributeContainer attributes) {
		attributes.attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, binaryLinkage);
	}

	@Override
	public BinaryLinkage getValue() {
		return binaryLinkage;
	}

	@Override
	public CoordinateAxis<BinaryLinkage> getAxis() {
		return BINARY_LINKAGE_COORDINATE_AXIS;
	}

	@Override
	public String toString() {
		return binaryLinkage.getName();
	}
}
