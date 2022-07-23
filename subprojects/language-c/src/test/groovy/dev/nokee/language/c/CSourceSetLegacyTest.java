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
package dev.nokee.language.c;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.testers.LanguageSourceSetLegacyTester;
import dev.nokee.language.c.internal.plugins.LegacyCSourceSet;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;

import java.io.File;

import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;

class CSourceSetLegacyTest extends LanguageSourceSetLegacyTester<CSourceSet> {
	@Override
	public CSourceSet createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply("dev.nokee.c-language-base");
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		return registry.register(newEntity("test", LegacyCSourceSet.class)).as(CSourceSet.class).get();
	}

	@Override
	public CSourceSet createSubject(File temporaryDirectory) {
		val project = ProjectTestUtils.createRootProject(temporaryDirectory);
		project.getPluginManager().apply("dev.nokee.c-language-base");
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		return registry.register(newEntity("test", LegacyCSourceSet.class)).as(CSourceSet.class).get();
	}
}
