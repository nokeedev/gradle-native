package dev.nokee.platform.base;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.BaseComponent;

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
	default BinaryView<Binary> getBinaries() {
		return ModelNodes.of(this).get(BaseComponent.class).getBinaries();
	}
}
