package dev.nokee.model.internal.core;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.registry.ManagedModelProjection;
import dev.nokee.model.internal.registry.MemoizedModelProjection;
import dev.nokee.model.internal.registry.UnmanagedCreatingModelProjection;
import dev.nokee.model.internal.registry.UnmanagedInstanceModelProjection;
import dev.nokee.model.internal.type.ModelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModelRegistration<T> {
	private final List<ModelProjection> projections;
	private final ModelPath path;
	private final ModelType<T> defaultProjectionType;

	private ModelRegistration(ModelPath path, ModelType<T> defaultProjectionType, List<ModelProjection> projections) {
		this.path = path;
		this.defaultProjectionType = defaultProjectionType;
		this.projections = projections;
	}

	// TODO: We should remove this...
	public ModelIdentifier<T> getIdentifier() {
		return ModelIdentifier.of(path, defaultProjectionType);
	}

	public ModelPath getPath() {
		return path;
	}

	public ModelType<T> getType() {
		return defaultProjectionType;
	}

	public List<ModelProjection> getProjections() {
		return projections;
	}

	public ModelRegistration<T> withProjections(List<ModelProjection> projections) {
		return new ModelRegistration<>(path, defaultProjectionType, projections);
	}

	public static <T> ModelRegistration<T> of(String path, Class<T> type) {
		return builder()
			.withPath(ModelPath.path(path))
			.withDefaultProjectionType(ModelType.of(type))
			.withProjection(ManagedModelProjection.of(type))
			.build();
	}

	public static <T> ModelRegistration<T> bridgedInstance(ModelIdentifier<T> identifier, T instance) {
		return builder()
			.withPath(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withProjection(UnmanagedInstanceModelProjection.of(instance))
			.build();
	}

	public static <T> ModelRegistration<T> unmanagedInstance(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withPath(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withProjection(new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(identifier.getType(), factory)))
			.build();
	}

	public static <T> Builder<T> unmanagedInstanceBuilder(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withPath(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withProjection(new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(identifier.getType(), factory)));
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static final class Builder<T> {
		private ModelPath path;
		private ModelType<? super T> defaultProjectionType = ModelType.untyped();
		private final List<ModelProjection> projections = new ArrayList<>();

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

		// take for granted that whatever projection type is, it will project to <T>
		@SuppressWarnings("unchecked")
		public ModelRegistration<T> build() {
			return new ModelRegistration<>(path, (ModelType<T>)defaultProjectionType, projections);
		}
	}
}
