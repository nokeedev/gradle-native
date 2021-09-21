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
package dev.nokee.scripts.testing;

import org.gradle.api.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public final class DefaultImporterMatchers {
	private DefaultImporterMatchers() {}

	public static Matcher<Project> hasDefaultImportFor(Class<?> type) {
		return new TypeSafeMatcher<Project>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("default import of " + type);
			}

			@Override
			protected boolean matchesSafely(Project item) {
				return Objects.equals(item.getExtensions().findByName(type.getSimpleName()), type);
			}
		};
	}
}
