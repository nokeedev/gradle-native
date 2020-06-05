package dev.nokee.platform.base;

import org.gradle.api.provider.Provider;

/**
 * A variant realization of a component.
 *
 * @since 0.2
 */
public interface Variant extends BinaryAwareComponent {
	/**
	 * Configure the binaries of this variant.
	 * The view contains only the binaries participating to this variant.
	 *
	 * @return a {@link BinaryView} for configuring each binary, never null.
	 * @since 0.4
	 */
	BinaryView<Binary> getBinaries();

	/**
	 * Returns a the development binary for this variant.
	 * The development binary is used by lifecycle tasks.
	 *
	 * @return a provider for the development binary, never null.
	 * @since 0.4
	 */
	Provider<Binary> getDevelopmentBinary();
}
