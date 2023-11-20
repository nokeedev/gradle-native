/*
 * Copyright 2020 the original author or authors.
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

import lombok.val;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A group of common model node operations.
 */
public final class ModelNodes {
	private ModelNodes() {}

	/**
	 * Returns the decorated model node of the specified instance.
	 * It is safe to say that for managed model node projection instance, a model node will be present.
	 *
	 * @param target  the instance to cross-reference with it's model node
	 * @return the model node for the specified instance if available
	 */
	public static ModelNode of(Object target) {
		return safeOf(target).orElseThrow(() -> objectNotDecoratedWithModelNode(target));
	}

	public static Optional<ModelNode> safeOf(Object target) {
		requireNonNull(target);
		if (target instanceof ModelNodeAware) {
			return Optional.of(((ModelNodeAware) target).getNode());
		} else if (target instanceof ExtensionAware) {
			return Optional.ofNullable(((ExtensionAware) target).getExtensions().findByType(ModelNode.class));
		} else if (target instanceof ModelNode) {
			return Optional.of((ModelNode) target);
		}
		return Optional.empty();
	}

	public static <T> T inject(T target, ModelNode node) {
		requireNonNull(target);
		requireNonNull(node);
		assert target instanceof ExtensionAware;
		val extensions = ((ExtensionAware) target).getExtensions();
		val currentNode = extensions.findByName("__NOKEE_modelNode");
		if (currentNode == null) {
			((ExtensionAware) target).getExtensions().add(ModelNode.class, "__NOKEE_modelNode", node);
		} else if (currentNode != node) {
			throw new IllegalStateException("Injecting a different model node!");
		}
		return target;
	}

	private static IllegalArgumentException objectNotDecoratedWithModelNode(Object target) {
		return new IllegalArgumentException(String.format("Object '%s' is not decorated with ModelNode.", target));
	}
}
