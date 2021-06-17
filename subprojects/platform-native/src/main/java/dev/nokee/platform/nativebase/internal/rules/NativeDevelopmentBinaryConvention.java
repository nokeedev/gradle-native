package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.runtime.nativebase.internal.DefaultBinaryLinkage;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static dev.nokee.platform.base.internal.DevelopmentBinaryUtils.selectSingleBinaryByType;

public enum NativeDevelopmentBinaryConvention implements Transformer<Provider<? extends Binary>, Iterable<? extends Binary>> {
	EXECUTABLE(ExecutableBinary.class),
	SHARED(SharedLibraryBinary.class),
	STATIC(StaticLibraryBinary.class);

	private final Class<? extends Binary> binaryTypeToSelect;

	NativeDevelopmentBinaryConvention(Class<? extends Binary> binaryTypeToSelect) {
		this.binaryTypeToSelect = binaryTypeToSelect;
	}

	@Override
	public Provider<? extends Binary> transform(Iterable<? extends Binary> binaries) {
		return selectSingleBinaryByType(binaryTypeToSelect, binaries);
	}

	public static NativeDevelopmentBinaryConvention of(DefaultBinaryLinkage linkage) {
		if (linkage.isExecutable()) {
			return EXECUTABLE;
		} else if (linkage.isShared()) {
			return SHARED;
		} else if (linkage.isStatic()) {
			return STATIC;
		}
		throw new IllegalArgumentException(String.format("Unsupported binary linkage '%s' for native development binary convention", linkage.getName()));
	}
}
