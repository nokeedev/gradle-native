package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.List;

public abstract class DefaultIosApplicationVariant extends BaseNativeVariant implements IosApplication {
	@Inject
	public DefaultIosApplicationVariant(String name, NamingScheme names, BuildVariant buildVariant, DefaultNativeComponentDependencies dependencies) {
		super(name, names, buildVariant);
	}

	@Override
	public Provider<? extends NativeBinary> getDevelopmentBinary() {
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
