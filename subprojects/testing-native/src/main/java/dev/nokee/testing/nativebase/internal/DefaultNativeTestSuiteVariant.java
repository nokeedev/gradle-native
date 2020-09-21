package dev.nokee.testing.nativebase.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
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
	public DefaultNativeTestSuiteVariant(VariantIdentifier<DefaultNativeTestSuiteVariant> identifier, NamingScheme names, VariantComponentDependencies<?> variantDependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers, BinaryView<Binary> binaryView) {
		super(identifier, names, objects, tasks, providers, binaryView);
		this.resolvableDependencies = variantDependencies.getIncoming();
	}
}
