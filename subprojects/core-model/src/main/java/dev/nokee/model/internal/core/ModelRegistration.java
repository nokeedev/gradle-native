package dev.nokee.model.internal.core;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.registry.MemoizedModelProjection;
import dev.nokee.model.internal.registry.UnmanagedCreatingModelProjection;
import dev.nokee.model.internal.registry.UnmanagedInstanceModelProjection;
import dev.nokee.model.internal.type.ModelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class ModelRegistration<T> {
	private final ModelIdentifier<T> identifier;
	private final List<ModelProjection> projections;

	private ModelRegistration(ModelIdentifier<T> identifier) {
		this(identifier, Collections.emptyList());
	}

	private ModelRegistration(ModelIdentifier<T> identifier, List<ModelProjection> projections) {
		this.identifier = requireNonNull(identifier);
		this.projections = projections;
	}

	public ModelIdentifier<T> getIdentifier() {
		return identifier;
	}

	public ModelPath getPath() {
		return identifier.getPath();
	}

	public ModelType<T> getType() {
		return identifier.getType();
	}

	public List<ModelProjection> getProjections() {
		return projections;
	}

	public ModelRegistration<T> withProjections(List<ModelProjection> projections) {
		return new ModelRegistration<>(identifier, projections);
	}

	public static <T> ModelRegistration<T> of(String path, Class<T> type) {
		return new ModelRegistration<>(ModelIdentifier.of(ModelPath.path(path), ModelType.of(type)));
	}

	public static <T> ModelRegistration<T> bridgedInstance(ModelIdentifier<T> identifier, T instance) {
		return builder()
			.withPath(identifier.getPath())
			.withMainProjectionType(identifier.getType())
			.withProjection(UnmanagedInstanceModelProjection.of(instance))
			.build();
	}

	public static <T> ModelRegistration<T> unmanagedInstance(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withPath(identifier.getPath())
			.withMainProjectionType(identifier.getType())
			.withProjection(new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(identifier.getType(), factory)))
			.build();
	}

	public static <T> Builder<T> unmanagedInstanceBuilder(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withPath(identifier.getPath())
			.withMainProjectionType(identifier.getType())
			.withProjection(new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(identifier.getType(), factory)));
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static final class Builder<T> {
		private ModelPath path;
		private ModelType<T> type;
		private final List<ModelProjection> projections = new ArrayList<>();

		public Builder<T> withPath(ModelPath path) {
			this.path = path;
			return this;
		}

		public <S> Builder<S> withMainProjectionType(ModelType<S> type) {
			((Builder<S>) this).type = type;
			return (Builder<S>) this;
		}

		public Builder<T> withProjection(ModelProjection projection) {
			projections.add(Objects.requireNonNull(projection));
			return this;
		}

		public ModelRegistration<T> build() {
			return new ModelRegistration<>(ModelIdentifier.of(path, type), projections);
		}
	}
}
