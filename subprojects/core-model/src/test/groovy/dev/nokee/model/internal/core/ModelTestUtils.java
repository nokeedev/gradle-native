package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import java.util.function.Consumer;

public final class ModelTestUtils {
	private static final String DEFAULT_NODE_NAME = "test";
	private static final ModelNode ROOT = rootNode();
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

	public static ModelNode rootNode() {
		return ModelNode.builder().withPath(ModelPath.root()).build();
	}

	public static ModelNode node(ModelNodeListener listener) {
		return childNode(ROOT, DEFAULT_NODE_NAME, builder -> builder.withListener(listener));
	}

	public static ModelNode node(ModelProjection... projections) {
		return childNode(ROOT, DEFAULT_NODE_NAME, builder -> builder.withProjections(projections));
	}

	public static ModelNode node(String path, ModelProjection... projections) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, builder -> builder.withProjections(projections));
		}
		return result;
	}

	public static ModelNode childNode(ModelNode parent) {
		return childNode(parent, DEFAULT_NODE_NAME);
	}

	public static ModelNode childNode(ModelNode parent, String name) {
		return childNode(parent, name, builder -> {});
	}

	public static ModelNode childNode(ModelNode parent, String name, Consumer<ModelNode.Builder> action) {
		val builder = ModelNode.builder();
		builder.withPath(parent.getPath().child(name));
		builder.withLookup(new ModelLookup() {
			@Override
			public ModelNode get(ModelPath path) {
				if (parent.getPath().equals(path)) {
					return parent;
				}
				throw new UnsupportedOperationException();
			}

			@Override
			public Result query(ModelSpec spec) {
				throw new UnsupportedOperationException();
			}
		});
		action.accept(builder);
		return builder.build();
	}
}
