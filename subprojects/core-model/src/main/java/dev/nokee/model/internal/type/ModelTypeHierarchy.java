/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal.type;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class ModelTypeHierarchy {
	private final Strategy strategy;
	private final Visitor visitor;

	private ModelTypeHierarchy(Strategy strategy, Visitor visitor) {
		this.strategy = strategy;
		this.visitor = visitor;
	}

	public void walk(ModelType<?> type) {
		final Set<ModelType<?>> seenTypes = new HashSet<>();
		final Deque<ModelType<?>> queue = new ArrayDeque<>();
		queue.add(type);
		while (!queue.isEmpty()) {
			final ModelType<?> walkingType = queue.removeFirst();
			if (seenTypes.add(walkingType)) {
				visitor.visitType(walkingType);
				strategy.walkType(walkingType, queue);
			}
		}
	}

	public static ModelTypeHierarchy siblingFirst(Visitor visitor) {
		return new ModelTypeHierarchy(Strategies.SiblingFirstStrategy, visitor);
	}

	public static ModelTypeHierarchy supertypeFirst(Visitor visitor) {
		return new ModelTypeHierarchy(Strategies.SupertypeFirstStrategy, visitor);
	}

	public interface Visitor {
		void visitType(ModelType<?> type);
	}

	private interface Strategy {
		void walkType(ModelType<?> walkingType, Deque<ModelType<?>> queue);
	}

	private enum Strategies implements Strategy {
		SiblingFirstStrategy {
			@Override
			public void walkType(ModelType<?> walkingType, Deque<ModelType<?>> queue) {
				walkingType.getSupertype().ifPresent(queue::addLast);
				queue.addAll(walkingType.getInterfaces());
			}
		},
		SupertypeFirstStrategy {
			@Override
			public void walkType(ModelType<?> walkingType, Deque<ModelType<?>> queue) {
				walkingType.getSupertype().ifPresent(queue::addFirst);
				queue.addAll(walkingType.getInterfaces());
			}
		}
	}
}
