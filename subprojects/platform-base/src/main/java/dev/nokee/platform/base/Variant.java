package dev.nokee.platform.base;

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
}
