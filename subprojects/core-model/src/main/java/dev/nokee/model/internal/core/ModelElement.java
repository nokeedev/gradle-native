/*
 * Copyright 2021 the original author or authors.
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

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.Named;

import java.lang.reflect.Type;

public interface ModelElement extends Named {
	default <S> DomainObjectProvider<S> as(Class<S> type) {
		return as(ModelType.of(type));
	}
	<S> DomainObjectProvider<S> as(ModelType<S> type);

	default boolean instanceOf(Type type) {
		return instanceOf(ModelType.of(type));
	}
	boolean instanceOf(ModelType<?> type);

	ModelElement property(String name);

	<S> DomainObjectProvider<S> element(String name, Class<S> type);
	<S> DomainObjectProvider<S> element(String name, ModelType<S> type);

	<S> ModelElement configure(ModelType<S> type, Action<? super S> action);
	default <S> ModelElement configure(Class<S> type, Action<? super S> action) {
		return configure(ModelType.of(type), action);
	}

	<S> DomainObjectProvider<S> mixin(ModelType<S> type);
}
