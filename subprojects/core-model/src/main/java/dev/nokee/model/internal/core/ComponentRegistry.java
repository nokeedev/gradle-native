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
package dev.nokee.model.internal.core;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public interface ComponentRegistry {
	@Nullable
	Component set(Entity.Id entityId, Component.Id componentId, Component component);

	@Nullable
	Component get(Entity.Id entityId, Component.Id componentId);

	Set<? extends Component.Id> getAllIds(Entity.Id entityId);
	Collection<Component> getAll(Entity.Id entityId);
}
