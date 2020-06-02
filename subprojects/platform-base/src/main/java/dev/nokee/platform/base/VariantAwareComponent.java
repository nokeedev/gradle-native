package dev.nokee.platform.base;

/**
 * A component with variants.
 *
 * @param <T> type of each variant
 * @since 0.4
 */
public interface VariantAwareComponent<T extends Variant> {
	/**
	 * Configure the variants of this component.
	 *
	 * @return a {@link VariantView} for configuring each variant of type {@code <T>}, never null.
	 */
	VariantView<T> getVariants();
}
