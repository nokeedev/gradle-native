package dev.nokee.platform.base;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.BaseComponent;

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
	default VariantView<T> getVariants() {
		return (VariantView<T>) ModelNodes.of(this).get(BaseComponent.class).getVariants();
	}
}
