package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;

public interface VariantInternal extends Variant {
	@Override
	BuildVariantInternal getBuildVariant();

	ResolvableComponentDependencies getResolvableDependencies();
}
