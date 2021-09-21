/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.internal;

import java.util.concurrent.Callable;

public final class ConventionalRelativeLanguageSourceSetPath implements Callable<String> {
	private final String relativePath;

	private ConventionalRelativeLanguageSourceSetPath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String get() {
		return relativePath;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String call() throws Exception {
		return relativePath;
	}

	public static final class Builder {
		private String componentName;
		private String sourceSetName;

		public Builder withComponentName(String componentName) {
			this.componentName = componentName;
			return this;
		}

		public Builder withSourceSetName(String sourceSetName) {
			this.sourceSetName = sourceSetName;
			return this;
		}

		public ConventionalRelativeLanguageSourceSetPath build() {
			assert componentName != null;
			assert sourceSetName != null;
			return new ConventionalRelativeLanguageSourceSetPath(asConventionalRelativePath(componentName, sourceSetName));
		}
	}

	private static String asConventionalRelativePath(String componentName, String sourceSetName) {
		return "src/" + componentName + "/" + sourceSetName;
	}
}
