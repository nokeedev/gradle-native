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

import dev.nokee.model.internal.core.ModelNode;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A read-only supplier of {@link ModelNode}.
 */
public interface ModelLookup {
	// TODO: We could introduce a visitor pattern API to walk model nodes.
	//   val result = ImmutableSet.<T>builder();
	//   visit(directDescendants(projection(type).andThen(result::add)));

	/**
	 * A model lookup query result.
	 */
	interface Result {
		/**
		 * Returns all model nodes matching the query.
		 *
		 * @return a list of matching nodes, never null.
		 */
		List<ModelNode> get();

		/**
		 * Returns the result mapped using the specified mapper function.
		 *
		 * @param mapper  mapper function to apply on each entity, must not be null
		 * @param <R> mapped type
		 * @return a list of the matching nodes mapped using the mapper function, never null.
		 */
		<R> List<R> map(Function<? super ModelNode, R> mapper);

		/**
		 * Performs the given action for each element of the results.
		 *
		 * @param action  the action to be performed for each element
		 */
		void forEach(Consumer<? super ModelNode> action);

		/**
		 * Returns an empty query result.
		 *
		 * @return an empty query result, never null.
		 */
		static Result empty() {
			return ModelLookupEmptyResult.INSTANCE;
		}
	}

	/**
	 * Returns an always failing model lookup.
	 *
	 * @return a model lookup that always fails.
	 */
	static ModelLookup failingLookup() {
		return new ModelLookup() {
		};
	}
}
