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
package dev.nokee.language.cpp;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(CppSourceSet.class)
class CppSourceSetTest extends LanguageSourceSetTester<CppSourceSet> {
	@Override
	public CppSourceSet createSubject() {
		return create(sourceSet("test", CppSourceSet.class)).as(CppSourceSet.class).get();
	}

	@Override
	public CppSourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", CppSourceSet.class)).as(CppSourceSet.class).get();
	}
}
