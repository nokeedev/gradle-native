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
package dev.gradleplugins.dockit.manual;

import dev.gradleplugins.dockit.common.TaskNameFactory;

final class Names implements TaskNameFactory {
	private final String name;

	Names(String name) {
		this.name = name;
	}

	public String taskName(String name) {
		return name + capitalized(this.name);
	}

	public String taskName(String verb, String object) {
		return verb + capitalized(name) + capitalized(object);
	}

	public String artifactName(String name) {
		return this.name + capitalized(name);
	}

	private static String capitalized(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
