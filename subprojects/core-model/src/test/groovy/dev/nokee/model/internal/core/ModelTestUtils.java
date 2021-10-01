/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelNodeBackedProvider;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelActions.initialize;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class ModelTestUtils {
	private static final Consumer<ModelNode.Builder> DO_NOTHING = builder -> {};
	private static final String DEFAULT_NODE_NAME = "test";
	private static final ModelNode ROOT = rootNode();
	private ModelTestUtils() {}

	public static ModelProjection projectionOf(Class<?> projectionType) {
		return ModelProjections.ofInstance(objectFactory().newInstance(projectionType));
	}

	public static ModelNode rootNode() {
		return ModelNode.builder().withPath(ModelPath.root()).withInstantiator(objectFactory()::newInstance).build();
	}

	public static ModelNode node(ModelNodeListener listener) {
		return childNode(ROOT, DEFAULT_NODE_NAME, ImmutableList.of(), builder -> builder.withListener(listener));
	}

	public static ModelNode node(ModelProjection... projections) {
		return childNode(ROOT, DEFAULT_NODE_NAME, ImmutableList.of(addProjections(asList(projections))), DO_NOTHING);
	}

	public static ModelNode node(Object... projectionInstances) {
		return childNode(ROOT, DEFAULT_NODE_NAME, ImmutableList.of(addProjections(ofInstances(projectionInstances))), DO_NOTHING);
	}

	public static ModelNode node(String name, Consumer<? super ModelNode.Builder> action) {
		return childNode(ROOT, name, ImmutableList.of(), action);
	}

	public static ModelNode node(String name, ModelProjection projection, Consumer<? super ModelNode.Builder> action) {
		return childNode(ROOT, name, ImmutableList.of(addProjections(ImmutableList.of(projection))), action);
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
			result = childNode(result, name, ImmutableList.of(addProjections(ImmutableList.<ModelProjection>builder().add(projection).add(projections).build())), DO_NOTHING);
		}
		return result;
	}

	public static ModelNode node(String path, Class<?> projectionType, Class<?>... projectionTypes) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, ImmutableList.of(addProjections(ofTypes(ImmutableList.<Class<?>>builder().add(projectionType).add(projectionTypes).build()))), DO_NOTHING);
		}
		return result;
	}

	public static ModelNode node(String path, Object... projectionInstances) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, ImmutableList.of(addProjections(ofInstances(projectionInstances))), DO_NOTHING);
		}
		return result;
	}

	private static List<ModelProjection> ofInstances(Object... projectionInstances) {
		return stream(projectionInstances).map(ModelProjections::ofInstance).collect(toList());
	}

	private static List<ModelProjection> ofTypes(List<Class<?>> projectionTypes) {
		return projectionTypes.stream().map(ModelType::of).map(ModelProjections::managed).collect(toList());
	}

	private static ModelAction addProjections(List<ModelProjection> projections) {
		return initialize(context -> projections.forEach(context::withProjection));
	}

	public static ModelNode childNode(ModelNode parent) {
		return childNode(parent, DEFAULT_NODE_NAME);
	}

	public static ModelNode childNode(ModelNode parent, String name) {
		return childNode(parent, name, ImmutableList.of(), DO_NOTHING);
	}

	public static ModelNode childNode(ModelNode parent, String name, Consumer<? super ModelNode.Builder> action) {
		return childNode(parent, name, ImmutableList.of(), action);
	}

	public static ModelNode childNode(ModelNode parent, String name, List<ModelAction> providedActions, Consumer<? super ModelNode.Builder> action) {
		val nodeProvider = new MutableObject<ModelNode>();
		val children = new HashMap<ModelPath, ModelNode>();
		val builder = ModelNode.builder();
		val actions = new ArrayList<>(providedActions);
		builder.withPath(parent.getPath().child(name));
		builder.withLookup(new ModelLookup() {
			@Override
			public ModelNode get(ModelPath path) {
				if (parent.getPath().equals(path)) {
					return parent;
				}
				return children.computeIfAbsent(path, key -> {
					throw new UnsupportedOperationException("This instance always fails if path is not '" + parent + "' or any known direct children.");
				});
			}

			@Override
			public Result query(ModelSpec spec) {
				throw new UnsupportedOperationException("This instance always fails.");
			}

			@Override
			public boolean has(ModelPath path) {
				return children.containsKey(path);
			}

			@Override
			public boolean anyMatch(ModelSpec spec) {
				throw new UnsupportedOperationException("This instance always fails.");
			}
		});
		builder.withInstantiator(objectFactory()::newInstance);
		builder.withConfigurer(new ModelConfigurer() {
			@Override
			public void configure(ModelAction action) {
				actions.add(action);
				action.execute(nodeProvider.getValue());
			}
		});
		builder.withListener(new ModelNodeListener() {
			@Override
			public void created(ModelNode node) {
				nodeProvider.setValue(node);
				execute(actions, node);
			}

			@Override
			public void initialized(ModelNode node) {
				execute(actions, node);
			}

			@Override
			public void registered(ModelNode node) {
				execute(actions, node);
			}

			@Override
			public void realized(ModelNode node) {
				execute(actions, node);
			}

			@Override
			public void projectionAdded(ModelNode node) {
				// do nothing for now.
			}

			private void execute(List<ModelAction> actions, ModelNode node) {
				val size = actions.size();
				for (int i = 0; i < size; ++i) {
					actions.get(i).execute(node);
				}
			}
		});
		builder.withRegistry(new ModelRegistry() {
			@Override
			public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> DomainObjectProvider<T> register(NodeRegistration<T> registration) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> DomainObjectProvider<T> register(ModelRegistration<T> registration) {
				val childNode = childNode(nodeProvider.getValue(), registration.getPath().getName(), registration.getActions(), builder -> {});
				children.put(registration.getPath(), childNode);
				return new ModelNodeBackedProvider<>(registration.getDefaultProjectionType(), childNode);
			}
		});
		action.accept(builder);
		return builder.build();
	}
}
