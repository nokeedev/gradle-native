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
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelSpec;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A read-only supplier of {@link ModelNode}.
 */
public interface ModelLookup {
	/**
	 * Returns the model node for the specified path.
	 *
	 * @param path  a model node path to retrieve
	 * @return the {@link ModelNode} for the specified path, never null.
	 */
	ModelNode get(ModelPath path);

	/**
	 * Returns the model node for the specified path if present.
	 *
	 * @param path  a model node path to retrive, must not be null
	 * @return the {@link ModelNode} for the specified path if present, never null
	 */
	Optional<ModelNode> find(ModelPath path);

	/**
	 * Executes a complex query on the known model nodes.
	 *
	 * @param spec  the spec to match the nodes to select
	 * @return a result instance containing matching nodes, never null.
	 */
	Result query(ModelSpec spec);

	/**
	 * Returns true if node exists at the specified path, or false otherwise.
	 *
	 * @param path  a model node path to check
	 * @return true if a node exists at the specified path, or false otherwise.
	 */
	boolean has(ModelPath path);

	/**
	 * Returns true if any node matches the specified spec, or false otherwise.
	 *
	 * @param spec  a spec to match any model nodes
	 * @return true if a node matches the specified spec, of false otherwise.
	 */
	boolean anyMatch(ModelSpec spec);

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
			@Override
			public ModelNode get(ModelPath path) {
				throw new UnsupportedOperationException("This instance always fails.");
			}

			@Override
			public Optional<ModelNode> find(ModelPath path) {
				return Optional.empty();
			}

			@Override
			public Result query(ModelSpec spec) {
				throw new UnsupportedOperationException("This instance always fails.");
			}

			@Override
			public boolean has(ModelPath path) {
				throw new UnsupportedOperationException("This instance always fails.");
			}

			@Override
			public boolean anyMatch(ModelSpec spec) {
				throw new UnsupportedOperationException("This instance always fails.");
			}
		};
	}
}
