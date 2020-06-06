package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.BinaryAwareNativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.List;

public abstract class DefaultIosApplicationVariant extends BaseNativeVariant implements IosApplication {
	private final BinaryAwareNativeComponentDependencies dependencies;

	@Inject
	public DefaultIosApplicationVariant(String name, NamingScheme names, BuildVariant buildVariant, BinaryAwareNativeComponentDependencies dependencies) {
		super(name, names, buildVariant);
		this.dependencies = dependencies;
	}

	@Override
	public BinaryAwareNativeComponentDependencies getDependencies() {
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
