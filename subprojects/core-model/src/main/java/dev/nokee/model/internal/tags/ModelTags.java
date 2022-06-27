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

import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ModelTags {
	private static final ConcurrentMap<Class<? extends ModelTag>, ModelComponentTag<?>> tags = new ConcurrentHashMap<>();

	private ModelTags() {}

	private static <T extends ModelTag> ModelComponentTag<T> newTag(Class<T> type) {
		return new ModelComponentTag<>(type);
	}

	public static <T extends ModelTag> ModelComponentTag<T> tag(Class<T> type) {
		@SuppressWarnings("unchecked")
		final ModelComponentTag<T> result = (ModelComponentTag<T>) tags.computeIfAbsent(type, ModelTags::newTag);
		return result;
	}

	public static <T extends ModelTag> ModelComponentType<ModelComponentTag<T>> typeOf(Class<T> type) {
		@SuppressWarnings("unchecked")
		final ModelComponentType<ModelComponentTag<T>> result = (ModelComponentType<ModelComponentTag<T>>) tags.computeIfAbsent(type, ModelTags::newTag).getComponentType();
		return result;
	}

	public static <T extends ModelTag> ModelComponentReference<ModelComponentTag<T>> referenceOf(Class<T> type) {
		@SuppressWarnings("unchecked")
		final ModelComponentReference<ModelComponentTag<T>> result = ModelComponentReference.ofInstance((ModelComponentType<ModelComponentTag<T>>) tags.computeIfAbsent(type, ModelTags::newTag).getComponentType());
		return result;
	}
}
