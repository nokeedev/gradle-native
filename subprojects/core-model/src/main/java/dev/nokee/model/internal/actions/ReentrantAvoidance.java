/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import com.google.common.collect.Iterables;
import dev.nokee.model.internal.core.ModelEntityId;
import dev.nokee.model.internal.core.ModelNode;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

final class ReentrantAvoidance {
	private final Map<ModelEntityId, Deque<ModelNode>> scope = new HashMap<>();

	public Consumer<Iterable<ModelNode>> andDeferredActions(ModelNode entity, Consumer<? super Iterable<ModelNode>> action) {
		return actionEntities -> {
			if (scope.containsKey(entity.getId())) {
				throw new IllegalStateException();
			}
			val leftovers = new ArrayDeque<ModelNode>();
			scope.put(entity.getId(), leftovers);

			action.accept(Iterables.concat(actionEntities, new IterableOnce<>(new RemoveFirstIterator<>(leftovers))));

			scope.remove(entity.getId());
		};
	}

	public Consumer<ModelNode> ifPossible(ModelNode actionEntity, Consumer<ModelNode> action) {
		return entity -> {
			if (scope.containsKey(entity.getId())) {
				scope.get(entity.getId()).add(actionEntity);
			} else {
				action.accept(entity);
			}
		};
	}

	private static final class RemoveFirstIterator<T> implements Iterator<T> {
		private final Deque<T> values;

		private RemoveFirstIterator(Deque<T> values) {
			this.values = values;
		}

		@Override
		public boolean hasNext() {
			return !values.isEmpty();
		}

		@Override
		public T next() {
			return values.removeFirst();
		}
	}
}
