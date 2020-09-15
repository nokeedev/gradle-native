package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class BuildableDevelopmentVariantConvention<T extends VariantInternal> implements Callable<T> {
	private final DevelopmentVariantConvention<T> delegate;

	public BuildableDevelopmentVariantConvention(Supplier<Iterable<T>> variants) {
		this.delegate = new DevelopmentVariantConvention<>(variants);
	}

	@Override
	public T call() throws Exception {
		val result = delegate.call();
		if (isBuildable(result)) {
			return result;
		}
		return null;
	}

	public boolean isBuildable(T variant) {
		return variant.getBuildVariant().hasAxisOf(DefaultOperatingSystemFamily.HOST) && variant.getBuildVariant().hasAxisOf(DefaultMachineArchitecture.HOST);
	}
}
