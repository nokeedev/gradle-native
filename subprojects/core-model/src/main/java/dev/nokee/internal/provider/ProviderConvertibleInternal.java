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
package dev.nokee.internal.provider;

import dev.nokee.provider.ProviderConvertible;
import org.gradle.api.provider.Provider;

import java.util.concurrent.Callable;

/**
 * An object that can be converted to a {@link Provider} compatible with Gradle {@literal Callable} aware APIs.
 *
 * @param <T> type of value represented by the provider
 */
public interface ProviderConvertibleInternal<T> extends ProviderConvertible<T>, Callable<Object> {
	/**
	 * Gradle-compatible {@literal Callable} provider conversion.
	 *
	 * Some Gradle APIs, i.e. {@link org.gradle.api.Project#files(Object...)} and {@link org.gradle.api.Task#dependsOn(Object...)},
	 * accept {@link Callable} types as a legacy mechanic to the {@link Provider} API.
	 * In the majority of the cases, the API also accepts {@link Provider}.
	 * Gradle will unpack the {@link Callable} and use any resulting value.
	 * In our {@literal ProviderConvertible} case, it will result in an automatic {@link Provider} conversion.
	 *
	 * @return a {@link Provider}, never null
	 */
	@Override
	default Object call() {
		return asProvider();
	}
}
