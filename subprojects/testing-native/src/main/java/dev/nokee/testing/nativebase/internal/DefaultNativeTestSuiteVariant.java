package dev.nokee.testing.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;

import javax.inject.Inject;

public abstract class DefaultNativeTestSuiteVariant extends BaseNativeVariant implements NativeTestSuiteVariant {
	@Inject
	public DefaultNativeTestSuiteVariant(String name, NamingScheme names, BuildVariant buildVariant, VariantComponentDependencies<?> variantDependencies) {
		super(name, names, buildVariant);
	}
}
