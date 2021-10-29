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
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.language.base.testers.LanguageSourceSetTester;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import lombok.val;

import java.io.File;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.ProjectIdentifier.ofRootProject;

class CHeaderSetTest extends LanguageSourceSetTester<CHeaderSet> {
	@Override
	public CHeaderSet createSubject() {
		val objects = objectFactory();
		val registry = new DefaultModelRegistry(objects::newInstance);
		val factory = new CHeaderSetRegistrationFactory(new LanguageSourceSetRegistrationFactory(objects, registry, new SourceSetFactory(objects)));
		return registry.register(factory.create(LanguageSourceSetIdentifier.of(ofRootProject(), "test"))).as(CHeaderSet.class).get();
	}

	@Override
	public CHeaderSet createSubject(File temporaryDirectory) {
		val objects = ProjectTestUtils.createRootProject(temporaryDirectory).getObjects();
		val registry = new DefaultModelRegistry(objects::newInstance);
		val factory = new CHeaderSetRegistrationFactory(new LanguageSourceSetRegistrationFactory(objects, registry, new SourceSetFactory(objects)));
		return registry.register(factory.create(LanguageSourceSetIdentifier.of(ofRootProject(), "test"))).as(CHeaderSet.class).get();
	}
}
