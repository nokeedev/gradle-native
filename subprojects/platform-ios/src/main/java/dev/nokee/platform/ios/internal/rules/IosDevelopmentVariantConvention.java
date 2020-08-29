package dev.nokee.platform.ios.internal.rules;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.*;

// TODO: Consolidate with DevelopmentVariantConvention class
public class IosDevelopmentVariantConvention<T extends VariantInternal> implements Callable<T> {
	private final Supplier<Iterable<T>> variants;

	public IosDevelopmentVariantConvention(Supplier<Iterable<T>> variants) {
		this.variants = variants;
	}

	@Override
	public T call() throws Exception {
		return StreamSupport.stream(this.variants.get().spliterator(), false).min(preferHostOperatingSystemFamily().thenComparing(preferHostMachineArchitecture()).thenComparing(preferSharedBinaryLinkage()).thenComparing(preferDebugBuildType())).orElseThrow(() -> new Exception("No variants available."));
	}
}
