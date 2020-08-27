package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;

public interface VariantInternal extends Variant {
	@Override
	BuildVariantInternal getBuildVariant();
}
