package dev.nokee.testing.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeTestSuiteVariant extends BaseNativeVariant implements NativeTestSuiteVariant, VariantInternal {
	@Getter private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public DefaultNativeTestSuiteVariant(VariantIdentifier<DefaultNativeTestSuiteVariant> identifier, String name, NamingScheme names, BuildVariantInternal buildVariant, VariantComponentDependencies<?> variantDependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers) {
		super(identifier, name, names, buildVariant, objects, tasks, providers);
		this.resolvableDependencies = variantDependencies.getIncoming();
	}
}
