package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.VariantInternal;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.*;

public class DevelopmentVariantConvention<T extends VariantInternal> implements Callable<T> {
	private final Supplier<Iterable<T>> variants;

	public DevelopmentVariantConvention(Supplier<Iterable<T>> variants) {
		this.variants = variants;
	}

	@Override
	public T call() throws Exception {
		return StreamSupport.stream(this.variants.get().spliterator(), false).min(preferHostOperatingSystemFamily().thenComparing(preferHostMachineArchitecture()).thenComparing(preferSharedBinaryLinkage()).thenComparing(preferDebugBuildType())).orElseThrow(() -> new Exception("No variants available."));
	}
}
