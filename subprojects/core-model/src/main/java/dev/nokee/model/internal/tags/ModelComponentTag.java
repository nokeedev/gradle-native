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
package dev.nokee.model.internal.tags;

import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentType;

public final class ModelComponentTag<T extends ModelTag> implements ModelComponent {
	private final Class<T> tagType;

	ModelComponentTag(Class<T> tagType) {
		this.tagType = tagType;
	}

	@Override
	public ModelComponentType<?> getComponentType() {
		return ModelComponentType.componentOf(tagType);
	}
}
