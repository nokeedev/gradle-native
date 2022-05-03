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

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.type.ModelType;

public interface ModelRegistry {
	default <T> DomainObjectProvider<T> get(String path, Class<T> type) {
		return get(ModelIdentifier.of(ModelPath.path(path), ModelType.of(type)));
	}
	<T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier);

	ModelNode instantiate(ModelRegistration registration);
	ModelElement register(ModelRegistration registration);
}
