package dev.nokee.platform.base;

/**
 * A component with binaries.
 *
 * @since 0.4
 */
public interface BinaryAwareComponent {
	/**
	 * Returns the binaries for this component.
	 *
	 * @return a {@link BinaryView} for configuring each binary, never null.
	 */
	BinaryView<Binary> getBinaries();
}
