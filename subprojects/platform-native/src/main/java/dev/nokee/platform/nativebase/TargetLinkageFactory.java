package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetLinkage;

/**
 * A factory for creating {@link TargetLinkage} instances.
 *
 * @since 0.5
 */
public interface TargetLinkageFactory {
	/**
	 * Creates a shared linkage for building shared libraries.
	 *
	 * @return a {@link TargetLinkage} instance representing the shared linkage, never null.
	 */
	TargetLinkage getShared();

	/**
	 * Creates a static linkage for building static libraries.
	 *
	 * @return a {@link TargetLinkage} instance representing the static linkage, never null.
	 */
	TargetLinkage getStatic();
}
