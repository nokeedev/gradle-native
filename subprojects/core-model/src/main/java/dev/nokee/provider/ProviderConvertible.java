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
package dev.nokee.provider;

import org.gradle.api.provider.Provider;

/**
 * An object that can be converted to a {@link Provider}.
 *
 * @param <T> type of value represented by the provider
 */
public interface ProviderConvertible<T> {
	/**
	 * Returns a {@literal Provider} representing this object.
	 *
	 * @return a {@link Provider}, never null
	 */
	Provider<T> asProvider();
}
