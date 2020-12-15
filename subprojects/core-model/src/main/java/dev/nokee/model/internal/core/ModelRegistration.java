package dev.nokee.model.internal.core;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A model registration request.
 *
 * @param <T>  the default projection type, provided for type-safety.
 */
@EqualsAndHashCode
public final class ModelRegistration<T> {
	private final List<ModelProjection> projections;
	private final List<ModelAction> actions;
	private final ModelPath path;
	@EqualsAndHashCode.Exclude private final ModelType<T> defaultProjectionType;

	private ModelRegistration(ModelPath path, ModelType<T> defaultProjectionType, List<ModelProjection> projections, List<ModelAction> actions) {
		this.path = path;
		this.defaultProjectionType = defaultProjectionType;
		this.projections = projections;
		this.actions = actions;
	}

	public ModelPath getPath() {
		return path;
	}

	public ModelType<T> getDefaultProjectionType() {
		return defaultProjectionType;
	}

	public List<ModelProjection> getProjections() {
		return projections;
	}

	public static <T> ModelRegistration<T> of(String path, Class<T> type) {
		return builder()
			.withPath(ModelPath.path(path))
			.withDefaultProjectionType(ModelType.of(type))
			.withProjection(ModelProjections.managed(ModelType.of(type)))
			.build();
	}

	public static <T> ModelRegistration<T> bridgedInstance(ModelIdentifier<T> identifier, T instance) {
		return builder()
			.withPath(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withProjection(ModelProjections.ofInstance(instance))
			.build();
	}

	public static <T> ModelRegistration<T> unmanagedInstance(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withPath(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withProjection(ModelProjections.createdUsing(identifier.getType(), factory))
			.build();
	}

	public static <T> Builder<T> unmanagedInstanceBuilder(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withPath(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withProjection(ModelProjections.createdUsing(identifier.getType(), factory));
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public List<ModelAction> getActions() {
		return actions;
	}

	public static final class Builder<T> {
		private ModelPath path;
		private ModelType<? super T> defaultProjectionType = ModelType.untyped();
		private final List<ModelProjection> projections = new ArrayList<>();
		private final List<ModelAction> actions = new ArrayList<>();

		public Builder<T> withPath(ModelPath path) {
			this.path = path;
			return this;
		}

		@SuppressWarnings("unchecked") // take the specified information for granted
		public <S extends T> Builder<S> withDefaultProjectionType(ModelType<S> type) {
			((Builder<S>) this).defaultProjectionType = type;
			return (Builder<S>) this;
		}

		public Builder<T> withProjection(ModelProjection projection) {
			projections.add(Objects.requireNonNull(projection));
			return this;
		}

		public Builder<T> action(ModelAction action) {
			actions.add(action);
			return this;
		}

		// take for granted that whatever projection type is, it will project to <T>
		@SuppressWarnings("unchecked")
		public ModelRegistration<T> build() {
			return new ModelRegistration<>(path, (ModelType<T>)defaultProjectionType, projections, actions);
		}
	}
}
