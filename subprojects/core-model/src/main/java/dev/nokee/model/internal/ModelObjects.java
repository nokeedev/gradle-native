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

package dev.nokee.model.internal;

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Set;
import java.util.stream.Stream;

public interface ModelObjects {
	<T> void register(ModelMap<T> repository);

	void configureEach(Action<? super Object> action);
	// TODO: Should we use KnownModelObject or simply introduce a new model?
	void whenElementKnown(Action<? super KnownModelObject<? extends Object>> action);
	void whenElementFinalized(Action<? super Object> action);

	Stream<KnownModelObject<?>> parentsOf(ModelObjectIdentifier identifier);

	<T> Provider<Set<T>> get(Class<T> type);
	<T> Provider<Set<T>> get(Class<T> type, Spec<? super ModelObjectIdentity<?>> spec);
	<T> Provider<Set<KnownModelObject<T>>> getElements(Class<T> type, Spec<? super ModelObjectIdentity<?>> spec);
}
