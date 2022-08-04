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
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.testers.HasPublicTypeTester;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public abstract class LanguageSourceSetLegacyTester<T extends LanguageSourceSet> extends LanguageSourceSetContentTester<T> implements LanguageSourceSetEmptyTester<T>, LanguageSourceSetSourceDirectoryTester<T>, LanguageSourceSetFilterTester<T>, LanguageSourceSetBuildDependenciesTester<T>, LanguageSourceSetCommonUsageTester<T>, HasPublicTypeTester<T> {
	@Override
	public Object subject() {
		return createSubject();
	}

	@Test
	void canGetBackingNodeFromInstance() {
		assertThat(ModelNodes.of(createSubject()), notNullValue(ModelNode.class));
	}
}
