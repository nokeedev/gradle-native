package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.List;

public class DefaultIosApplicationVariant extends BaseNativeVariant implements IosApplication, VariantInternal {
	private final NativeComponentDependencies dependencies;

	@Inject
	public DefaultIosApplicationVariant(String name, NamingScheme names, BuildVariantInternal buildVariant, VariantComponentDependencies<NativeComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers) {
		super(name, names, buildVariant, objects, tasks, providers);
		this.dependencies = dependencies.getDependencies();
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected Provider<Binary> getDefaultBinary() {
		return getProviders().provider(() -> {
			List<? extends SignedIosApplicationBundleInternal> binaries = getBinaries().flatMap(it -> {
				if (it instanceof SignedIosApplicationBundleInternal) {
					return ImmutableList.of((SignedIosApplicationBundleInternal)it);
				}
				return ImmutableList.of();
			}).get();
			if (binaries.isEmpty()) {
				return null;
			}
			return one(binaries);
		});
	}
}
