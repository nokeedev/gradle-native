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
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface KnownElements {
	// TODO: Consider using NamedDomainObjectRegistry instead of Function
	<S> ModelObject<S> register(ModelObjectIdentifier identifier, Class<S> type, Function<? super String, ? extends NamedDomainObjectProvider<S>> factory);
	<S> S create(String name, Class<S> type, Function<? super ModelElement, ? extends S> factory);
	ModelMapAdapters.ModelElementIdentity getById(ModelObjectIdentifier identifier);
	@Nullable ModelElement findByName(String name);
	void forEach(Action<? super ModelMapAdapters.ModelElementIdentity> configureAction);

	<S> Action<S> forCreatedElements(Namer<S> namer, Function<? super String, NamedDomainObjectProvider<?>> query);
}
