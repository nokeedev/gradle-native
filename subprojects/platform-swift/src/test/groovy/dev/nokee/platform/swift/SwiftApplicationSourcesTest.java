/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.swift;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.swift.internal.plugins.SwiftApplicationPlugin.swiftApplication;

@Subject(SwiftApplicationSources.class)
class SwiftApplicationSourcesTest implements ComponentSourcesTester<SwiftApplicationSources> {
	@Override
	public SwiftApplicationSources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (SwiftApplicationSources) create(swiftApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("swift", SwiftSourceSet.class));
	}
}
