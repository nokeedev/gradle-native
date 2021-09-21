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
package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;

public interface LanguageSourceSetEmptyTester<T extends LanguageSourceSet> {
	T createSubject();

	@Test
	default void hasEmptyFileTreeWhenNoSource() {
		assertThat("file tree should be empty", createSubject().getAsFileTree(), emptyIterable());
	}

	@Test
	default void hasEmptySourceDirectoriesWhenNoSource() {
		assertThat("source directories should be empty", createSubject().getSourceDirectories(), emptyIterable());
	}

	@Test
	default void hasNoFilterPatternByDefault() {
		assertThat(createSubject().getFilter().getExcludes(), empty());
		assertThat(createSubject().getFilter().getIncludes(), empty());
	}
}
