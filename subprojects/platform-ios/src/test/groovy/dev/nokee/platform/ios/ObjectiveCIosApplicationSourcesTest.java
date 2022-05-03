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
package dev.nokee.platform.ios;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.ios.internal.plugins.IosComponentBasePlugin;
import dev.nokee.platform.ios.internal.plugins.IosResourcePlugin;
import lombok.val;

import java.util.stream.Stream;

import static dev.nokee.platform.ios.internal.plugins.ObjectiveCIosApplicationPlugin.objectiveCIosApplication;

class ObjectiveCIosApplicationSourcesTest implements ComponentSourcesTester<ObjectiveCIosApplicationSources> {
	@Override
	public ObjectiveCIosApplicationSources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(IosComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		project.getPluginManager().apply(IosResourcePlugin.class);
		val sources = project.getExtensions().getByType(ModelRegistry.class).register(objectiveCIosApplication("main", project)).as(ObjectiveCIosApplication.class).get().getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("objectiveC", ObjectiveCSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class), new SourceSetUnderTest("resources", IosResourceSet.class));
	}
}
