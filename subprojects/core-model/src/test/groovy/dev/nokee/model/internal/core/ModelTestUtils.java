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

import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

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

	public static ModelNode node(ModelProjection... projections) {
		val result = childNode(ROOT, DEFAULT_NODE_NAME, DO_NOTHING);
		stream(projections).forEach(result::addComponent);
		return result;
	}

	public static ModelNode node(Object... projectionInstances) {
		val result = childNode(ROOT, DEFAULT_NODE_NAME, DO_NOTHING);
		stream(projectionInstances).map(ModelProjections::ofInstance).forEach(result::addComponent);
		return result;
	}

	public static ModelNode node(String name, Consumer<? super ModelNode.Builder> action) {
		return childNode(ROOT, name, action);
	}

	public static ModelNode node(String name, ModelProjection projection, Consumer<? super ModelNode.Builder> action) {
		val result = childNode(ROOT, name, action);
		result.addComponent(projection);
		return result;
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
			result = childNode(result, name, DO_NOTHING);
			concat(of(projection), stream(projections)).forEach(result::addComponent);
		}
		return result;
	}

	public static ModelNode node(String path, Class<?> projectionType, Class<?>... projectionTypes) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, DO_NOTHING);
			concat(of(projectionType), stream(projectionTypes)).map(ModelType::of).map(ModelProjections::managed)
				.forEach(result::addComponent);
		}
		return result;
	}

	public static ModelNode node(String path, Object... projectionInstances) {
		ModelNode result = ROOT;
		for (String name : ModelPath.path(path)) {
			result = childNode(result, name, DO_NOTHING);
			stream(projectionInstances).map(ModelProjections::ofInstance).forEach(result::addComponent);
		}
		return result;
	}

	public static ModelNode childNode(ModelNode parent) {
		return childNode(parent, DEFAULT_NODE_NAME);
	}

	public static ModelNode childNode(ModelNode parent, String name) {
		return childNode(parent, name, DO_NOTHING);
	}

	public static ModelNode childNode(ModelNode parent, String name, Consumer<? super ModelNode.Builder> action) {
		val nodeProvider = new MutableObject<ModelNode>();
		val children = new HashMap<ModelPath, ModelNode>();
		val builder = ModelNode.builder();
		val actions = new ArrayList<ModelAction>();
		builder.withPath(ModelNodeUtils.getPath(parent).child(name));
		builder.withLookup(new ModelLookup() {
		});
		builder.withInstantiator(objectFactory()::newInstance);
		builder.withConfigurer(new ModelConfigurer() {
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
		builder.withRegistry(new ModelRegistry() {});
		action.accept(builder);
		return builder.build();
	}
}
