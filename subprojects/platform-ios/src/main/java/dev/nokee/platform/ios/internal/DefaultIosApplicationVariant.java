package dev.nokee.platform.ios.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultIosApplicationVariant extends BaseNativeVariant implements IosApplication, VariantInternal {
	private final DefaultNativeComponentDependencies dependencies;
	@Getter private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public DefaultIosApplicationVariant(VariantIdentifier<DefaultIosApplicationVariant> identifier, NamingScheme names, VariantComponentDependencies<DefaultNativeComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers, BinaryView<Binary> binaryView) {
		super(identifier, names, objects, tasks, providers, binaryView);
		this.dependencies = dependencies.getDependencies();
		this.resolvableDependencies = dependencies.getIncoming();

		getDevelopmentBinary().convention(getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}
}
