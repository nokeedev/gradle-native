package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.type.ModelType;

import java.util.Arrays;

public final class ModelTestUtils {
	private static final ModelPath DEFAULT_MODEL_PATH = ModelPath.path("test");
	private ModelTestUtils() {}

	public static ModelProjection projectionOf(Class<?> projectionType) {
		return new ModelProjection() {
			private final Object instance = TestUtils.objectFactory().newInstance(projectionType);

			@Override
			public <T> boolean canBeViewedAs(ModelType<T> type) {
				return ModelType.of(projectionType).isAssignableFrom(type);
			}

			@Override
			public <T> T get(ModelType<T> type) {
				return type.getConcreteType().cast(instance);
			}
		};
	}

	public static ModelNode node(ModelProjection... projections) {
		return new ModelNode(DEFAULT_MODEL_PATH, Arrays.asList(projections));
	}

	public static ModelNode node(String path, ModelProjection... projections) {
		return new ModelNode(ModelPath.path(path), Arrays.asList(projections));
	}
}
