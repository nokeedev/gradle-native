package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.registry.ManagedModelProjection;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.UnmanagedInstanceModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ModelTestUtils {
	private static final String DEFAULT_NODE_NAME = "test";
	private static final ModelNode ROOT = rootNode();
	private ModelTestUtils() {}

	public static ModelProjection projectionOf(Class<?> projectionType) {
		return UnmanagedInstanceModelProjection.of(TestUtils.objectFactory().newInstance(projectionType));
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

	public static ModelNode node(Object... projectionInstances) {
		return childNode(ROOT, DEFAULT_NODE_NAME, builder -> builder.withProjections(Arrays.stream(projectionInstances).map(UnmanagedInstanceModelProjection::of).collect(Collectors.toList())));
	}

	public static ModelNode node(String name, Consumer<? super ModelNode.Builder> action) {
		return childNode(ROOT, name, action);
	}

	public static ModelNode node(String path) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name);
		}
		return result;
	}

	public static ModelNode node(String path, ModelProjection projection, ModelProjection... projections) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, builder -> builder.withProjections(ImmutableList.<ModelProjection>builder().add(projection).add(projections).build()));
		}
		return result;
	}

	public static ModelNode node(String path, Class<?> projectionType, Class<?>... projectionTypes) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, builder -> builder.withProjections(ImmutableList.<Class<?>>builder().add(projectionType).add(projectionTypes).build().stream().map(ModelType::of).map(ManagedModelProjection::of).collect(Collectors.toList())));
		}
		return result;
	}

	public static ModelNode node(String path, Object... projectionInstances) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, builder -> builder.withProjections(Arrays.stream(projectionInstances).map(UnmanagedInstanceModelProjection::of).collect(Collectors.toList())));
		}
		return result;
	}

	public static ModelNode childNode(ModelNode parent) {
		return childNode(parent, DEFAULT_NODE_NAME);
	}

	public static ModelNode childNode(ModelNode parent, String name) {
		return childNode(parent, name, builder -> {});
	}

	public static ModelNode childNode(ModelNode parent, String name, Consumer<? super ModelNode.Builder> action) {
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

			@Override
			public boolean has(ModelPath path) {
				throw new UnsupportedOperationException();
			}
		});
		action.accept(builder);
		return builder.build();
	}
}
