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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.nativebase.HasPrivateHeaders;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;

public interface PrivateHeadersMixIn extends HasPrivateHeaders {
	ConfigurableFileCollection getPrivateHeaders();

	@Override
	default void privateHeaders(Action<? super ConfigurableFileCollection> action) {
		action.execute(getPrivateHeaders());
	}

	@Override
	default void privateHeaders(@SuppressWarnings("rawtypes") Closure closure) {
		privateHeaders(new ClosureWrappedConfigureAction<>(closure));
	}
}
