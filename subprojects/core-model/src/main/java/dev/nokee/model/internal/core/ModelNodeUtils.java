/*
 * Copyright 2021 the original author or authors.
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

import dev.nokee.model.internal.type.ModelType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// TODO: Remove "maybe add" custom logic to favour dedup within ModelProjection adding logic
public final class ModelNodeUtils {

	private ModelNodeUtils() {}

	/**
	 * Returns the parent node of the specified node, if available.
	 *
	 * @param self  the node to query parent, must not be null
	 * @return the parent model node, never null but can be absent
	 */
	public static Optional<ModelNode> getParent(ModelNode self) {
		return self.find(ParentComponent.class).map(ParentComponent::get);
	}

	public static void setParent(ModelNode self, ModelNode parent) {
		self.addComponent(new ParentComponent(parent));
	}

	/**
	 * Returns the direct descending nodes.
	 *
	 * @param self  the node to query direct descendants, must not be null
	 * @return a list of directly descending nodes, never null.
	 */
	public static List<ModelNode> getDirectDescendants(ModelNode self) {
		return self.get(DescendantNodes.class).getDirectDescendants();
	}

	public static ModelNode getDescendant(ModelNode self, String name) {
		return self.get(DescendantNodes.class).getDescendant(name);
	}

	public static boolean hasDescendant(ModelNode self, String name) {
		return self.get(DescendantNodes.class).hasDescendant(name);
	}

	/**
	 * Returns if the current node can be viewed as the specified type.
	 *
	 * @param self  the node to check viewing, must not be null
	 * @param type  the type to query this model node
	 * @return true if the node can be projected into the specified type, or false otherwise.
	 */
	public static boolean canBeViewedAs(ModelNode self, ModelType<?> type) {
		return self.getComponentBits().containsAll(ModelComponentType.projectionOf(type.getConcreteType()).familyBits());
	}

	public static Stream<ModelProjection> getProjections(ModelNode self) {
		return self.getComponents().filter(ModelProjection.class::isInstance).map(ModelProjection.class::cast);
	}

	/**
	 * Returns the main projection type description of this node.
	 * In practice, this describes the type of the Object projection of this node.
	 *
	 * @param self  the node to query type description, must not be null
	 * @return the type description of this node, if present.
	 */
	// TODO: This is used to generating descriptive message. We should find a better way to do this.
	//  In the end, this fits in the reporting APIs.
	public static Optional<String> getTypeDescription(ModelNode self) {
		return getProjections(self).findFirst().map(ModelProjection::getTypeDescriptions).map(it -> String.join(", ", it));
	}

	/**
	 * Returns the first projection matching the specified type.
	 *
	 * @param self  the node to query projection, must not be null
	 * @param type  the type of the requested projection
	 * @param <T>  the type of the requested projection
	 * @return an instance of the projected node into the specified instance
	 * @see #get(ModelNode, ModelType)
	 */
	public static <T> T get(ModelNode self, Class<T> type) {
		return get(self, ModelType.of(type));
	}

	/**
	 * Returns the first projection matching the specified type.
	 *
	 * @param self  the node to query projection, must not be null
	 * @param type  the type of the requested projection
	 * @param <T>  the type of the requested projection
	 * @return an instance of the projected node into the specified instance
	 */
	public static <T> T get(ModelNode self, ModelType<T> type) {
		return ModelNodeContext.of(self).execute(node -> {
			return getProjections(node)
				.filter(it -> it.canBeViewedAs(type))
				.findFirst().map(it -> it.get(type))
				.orElseThrow(() -> new IllegalStateException("no projection for " + type));
		});
	}

	public static void finalizeProjections(ModelNode self) {
		getProjections(self).filter(it -> it.canBeViewedAs(ModelType.of(Finalizable.class)))
			.forEach(it -> it.get(ModelType.of(Finalizable.class)).finalizeValue());
	}

	public static void applyTo(ModelNode self, NodeAction action) {
		self.get(RelativeConfigurationService.class).applyTo(action);
	}

	public static ModelNode instantiate(ModelNode self, ModelRegistration registration) {
		return self.get(RelativeRegistrationService.class).instantiate(registration);
	}

	/**
	 * Returns the path of this model node.
	 *
	 * @param self  the entity to return the path
	 * @return a {@link ModelPath} representing this model node, never null.
	 */
	public static ModelPath getPath(ModelNode self) {
		return self.get(ModelPathComponent.class).get();
	}
}
