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
package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelNode;
import lombok.val;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ModelLookupReactiveResult implements ModelLookup.Result {
	private final List<ModelNode> liveEntities;
	private final Predicate<ModelNode> predicate;

	public ModelLookupReactiveResult(List<ModelNode> liveEntities, Predicate<ModelNode> predicate) {
		this.liveEntities = liveEntities;
		this.predicate = predicate;
	}

	@Override
	public List<ModelNode> get() {
		val result = ImmutableList.<ModelNode>builder();
		forEach(result::add);
		return result.build();
	}

	public <R> List<R> map(Function<? super ModelNode, R> mapper) {
		val result = ImmutableList.<R>builder();
		forEach(entity -> result.add(mapper.apply(entity)));
		return result.build();
	}

	public void forEach(Consumer<? super ModelNode> action) {
		for (int i = 0; i < liveEntities.size(); ++i) {
			if (predicate.test(liveEntities.get(i))) {
				action.accept(liveEntities.get(i));
			}
		}
	}
}
