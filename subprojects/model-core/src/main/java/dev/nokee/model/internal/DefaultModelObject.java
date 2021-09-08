package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.TypeAwareModelProjection;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
final class DefaultModelObject<T> extends AbstractModelObject<T> implements ModelObject<T> {
	@EqualsAndHashCode.Exclude private final ModelNode node;
	@EqualsAndHashCode.Include private final TypeAwareModelProjection<T> projection;

	public DefaultModelObject(TypeAwareModelProjection<T> projection) {
		this.node = projection.getOwner();
		this.projection = projection;
	}

	@Override
	protected ModelNode getNode() {
		return node;
	}

	@Override
	public TypeAwareModelProjection<T> getProjection() {
		if (projection == null) {
			return super.getProjection();
		}
		return projection;
	}
}
