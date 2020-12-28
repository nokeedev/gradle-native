package dev.nokee.language.base.internal;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelSpec;
import dev.nokee.model.internal.core.ModelSpecs;

import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.type.ModelType.of;

public interface SourceSetExtensible {
	static ModelSpec discoveringInstanceOf(Class<? extends SourceSetExtensible> markerInterfaceType) {
		return ModelSpecs.of(stateAtLeast(ModelNode.State.Registered).and(withType(of(markerInterfaceType))));
	}
}
