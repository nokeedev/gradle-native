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
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class ModelTestUtils {
	private static final Consumer<ModelNode.Builder> DO_NOTHING = builder -> {};
	private static final String DEFAULT_NODE_NAME = "test";
	private static final ModelNode ROOT = rootNode();
	private static final ModelElementFactory FACTORY = new ModelElementFactory(objectFactory()::newInstance);
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
		return new AddProjectionAction(projections);
	}

	public static final class AddProjectionAction implements ModelAction, HasInputs {
		private final List<ModelComponentReference<?>> inputs = ImmutableList.of(ModelComponentReference.of(ModelState.class), ModelComponentReference.of(BindManagedProjectionService.class));
		private final Bits inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		private final Iterable<ModelProjection> projections;

		public AddProjectionAction(Iterable<ModelProjection> projections) {
			this.projections = projections;
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				if (ModelStates.getState(node).equals(ModelState.Created)) {
					// NOTE: The contextual node should not be accessed from the action, it's simply for contextualizing the action execution.
					projections.forEach(node::addComponent);
				}
			}
		}

		@Override
		public List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}
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
		builder.withPath(ModelNodeUtils.getPath(parent).child(name));
		builder.withLookup(new ModelLookup() {
			@Override
			public ModelNode get(ModelPath path) {
				if (ModelNodeUtils.getPath(parent).equals(path)) {
					return parent;
				}
				return children.computeIfAbsent(path, key -> {
					throw new UnsupportedOperationException("This instance always fails if path is not '" + parent + "' or any known direct children.");
				});
			}

			@Override
			public Optional<ModelNode> find(ModelPath path) {
				if (ModelNodeUtils.getPath(parent).equals(path)) {
					return Optional.of(parent);
				}
				return Optional.ofNullable(children.get(path));
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
				val node = nodeProvider.getValue();
				if (node != null) {
					if (action instanceof HasInputs && ((HasInputs) action).getInputs().stream().allMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(node.getComponentTypes()))) {
						action.execute(node);
					} else {
						action.execute(node);
					}
				}
			}
		});
		builder.withListener(new ModelNodeListener() {
			@Override
			public void projectionAdded(ModelNode node, ModelComponent newComponent) {
				if (newComponent instanceof ModelState.IsAtLeastCreated) {
					nodeProvider.setValue(node);
				}
				val size = actions.size(); // avoid replaying new actions
				for (int i = 0; i < size; ++i) {
					val configuration = actions.get(i);
					if (configuration instanceof HasInputs) {
						if (((HasInputs) configuration).getInputs().stream().anyMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(ModelComponentType.ofInstance(newComponent))) && ((HasInputs) configuration).getInputs().stream().allMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(node.getComponentTypes()))) {
							configuration.execute(node);
						} else if (((HasInputs) configuration).getInputs().isEmpty()) {
							configuration.execute(node);
						}
					} else {
						configuration.execute(node);
					}
				}
			}
		});
		builder.withRegistry(new ModelRegistry() {
			@Override
			public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
				throw new UnsupportedOperationException();
			}

			@Override
			public ModelNode instantiate(ModelRegistration registration) {
				val path = registration.getComponents().stream().flatMap(this::findModelPath).findFirst().get();
				val childNode = childNode(nodeProvider.getValue(), path.getName(), registration.getActions(), builder -> {});
				registration.getComponents().forEach(it -> {
					if (it instanceof ModelProjection) {
						childNode.addComponent((ModelProjection) it);
					} else {
						childNode.addComponent((ModelComponent) it);
					}
				});
				children.put(path, childNode);
				return childNode;
			}

			@Override
			public ModelElement register(ModelRegistration registration) {
				return FACTORY.createElement(instantiate(registration));
			}

			private Stream<ModelPath> findModelPath(Object component) {
				if (component instanceof ModelPathComponent) {
					return Stream.of(((ModelPathComponent) component).get());
				}
				return Stream.empty();
			}
		});
		action.accept(builder);
		return builder.build();
	}
}
