package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.TypeAwareModelProjection;

final class DefaultModelObject<T> extends AbstractModelObject<T> implements ModelObject<T> {
	private final ModelNode node;
	private final TypeAwareModelProjection<T> projection;

	public DefaultModelObject(TypeAwareModelProjection<T> projection) {
		this.node = projection.getOwner();
		this.projection = projection;
	}

	@Override
	public TypeAwareModelProjection<T> getProjection() {
		if (projection == null) {
			return super.getProjection();
		}
		return projection;
	}
}
