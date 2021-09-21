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

import com.google.common.base.Predicates;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A predicate for matching model node more efficiently.
 */
// TODO: Maybe we could achieve a more minimalist API by using a raw-ish Predicate where the additional methods for efficiency are hidden from the users.
//   We will revisit this at a later time.
public interface ModelPredicate {
	default Optional<ModelPath> getPath() {
		return Optional.empty();
	}

	default Optional<ModelPath> getParent() {
		return Optional.empty();
	}

	default Optional<ModelPath> getAncestor() {
		return Optional.empty();
	}

	default Predicate<? super ModelNode> getMatcher() {
		return Predicates.alwaysTrue();
	}
}
