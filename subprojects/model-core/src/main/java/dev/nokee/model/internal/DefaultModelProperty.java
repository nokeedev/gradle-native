package dev.nokee.model.internal;

import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.TypeAwareModelProjection;
import org.gradle.api.Action;

import java.util.function.Consumer;

final class DefaultModelProperty<T> extends AbstractModelObject<T> implements ModelProperty<T> {
	private final TypeAwareModelProjection<T> projection;

	public DefaultModelProperty(TypeAwareModelProjection<T> projection) {
		this.projection = projection;
	}

	@Override
	public TypeAwareModelProjection<T> getProjection() {
		if (projection == null) {
			return super.getProjection();
		} else {
			return projection;
		}
	}

	@Override
	public ModelProperty<T> configure(Action<? super T> action) {
		return (ModelProperty<T>) super.configure(action);
	}

	@Override
	public ModelProperty<T> configure(Consumer<? super ModelObject<? extends T>> action) {
		return (ModelProperty<T>) super.configure(action);
	}
}
