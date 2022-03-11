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
package nokeebuild.licensing;

import org.gradle.api.Action;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

import java.util.Optional;

final class GradleExtensions {
	private final ExtensionContainer extensions;

	private GradleExtensions(ExtensionContainer extensions) {
		this.extensions = extensions;
	}

	public static GradleExtensions from(Object extensionAwareObject) {
		return new GradleExtensions(((ExtensionAware) extensionAwareObject).getExtensions());
	}

	public <T> void configure(String name, Action<? super T> action) {
		extensions.configure(name, action);
	}

	public <T> void configure(Class<T> type, Action<? super T> action) {
		extensions.configure(type, action);
	}

	public <T> void ifPresent(Class<T> type, Action<? super T> action) {
		Optional.ofNullable(extensions.findByType(type)).ifPresent(action::execute);
	}
}
