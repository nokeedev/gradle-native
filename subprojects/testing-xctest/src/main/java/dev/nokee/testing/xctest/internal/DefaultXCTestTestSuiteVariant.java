package dev.nokee.testing.xctest.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.List;

public class DefaultXCTestTestSuiteVariant extends BaseNativeVariant implements IosApplication {
	private final DefaultNativeComponentDependencies dependencies;

	@Inject
	public DefaultXCTestTestSuiteVariant(String name, NamingScheme names, BuildVariantInternal buildVariant, VariantComponentDependencies<DefaultNativeComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers) {
		super(name, names, buildVariant, objects, tasks, providers);
		this.dependencies = dependencies.getDependencies();
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
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
