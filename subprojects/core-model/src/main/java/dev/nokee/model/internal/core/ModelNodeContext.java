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

import lombok.val;
import org.gradle.api.plugins.ExtensionAware;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ModelNodeContext {
	private static final ThreadLocal<ModelNode> MODEL_NODE_INFO = new ThreadLocal<>();
	private final ModelNode node;

	private ModelNodeContext(ModelNode node) {
		this.node = requireNonNull(node);
	}

	public static ModelNodeContext of(ModelNode node) {
		return new ModelNodeContext(node);
	}

	public <T> T execute(Function<? super ModelNode, ? extends T> action) {
		val previousNode = MODEL_NODE_INFO.get();
		MODEL_NODE_INFO.set(node);
		try {
			return action.apply(node);
		} finally {
			MODEL_NODE_INFO.set(previousNode);
		}
	}

	public void execute(Runnable action) {
		val previousNode = MODEL_NODE_INFO.get();
		MODEL_NODE_INFO.set(node);
		try {
			action.run();
		} finally {
			MODEL_NODE_INFO.set(previousNode);
		}
	}

	public void execute(Consumer<? super ModelNode> action) {
		val previousNode = MODEL_NODE_INFO.get();
		MODEL_NODE_INFO.set(node);
		try {
			action.accept(node);
		} finally {
			MODEL_NODE_INFO.set(previousNode);
		}
	}

	public static ModelNode getCurrentModelNode() {
		return requireNonNull(MODEL_NODE_INFO.get(), "no mode node context");
	}

	public static <T> T injectCurrentModelNodeIfAllowed(T target) {
		requireNonNull(target);
		if (target instanceof ExtensionAware && canInjectCurrentModelNode()) {
			ModelNodes.inject(target, ModelNodeContext.getCurrentModelNode());
		}
		return target;
	}

	private static boolean canInjectCurrentModelNode() {
		return MODEL_NODE_INFO.get() != null;
	}
}
