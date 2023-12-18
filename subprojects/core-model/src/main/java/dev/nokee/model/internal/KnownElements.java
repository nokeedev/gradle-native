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

import dev.nokee.model.PolymorphicDomainObjectRegistry;
import org.gradle.api.Action;

public interface KnownElements {
	<ObjectType> ModelObject<ObjectType> register(ModelObjectIdentity<ObjectType> identity, PolymorphicDomainObjectRegistry<? super ObjectType> registry);

	void forEach(Action<? super ModelMapAdapters.ModelElementIdentity> configureAction);

	<T> ModelObject<T> getById(ModelObjectIdentifier identifier, Class<T> type);

	boolean isKnown(ModelObjectIdentifier identifier, Class<?> type);
}
