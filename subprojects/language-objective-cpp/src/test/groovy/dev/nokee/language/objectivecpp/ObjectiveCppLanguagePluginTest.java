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
package dev.nokee.language.objectivecpp;

import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetExtensible;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguagePlugin;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.internal.type.ModelType.of;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ObjectiveCppLanguagePlugin.class)
class ObjectiveCppLanguagePluginTest {
	@Test
	void appliesCppLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCppLanguagePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(ObjectiveCppLanguageBasePlugin.class), "should apply Objective-C++ language base plugin");
	}

	@Test
	void createsObjectiveCppSourceSetUponDiscoveringSourceSetExtensibleType() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCppLanguagePlugin.class));
		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
		modelRegistry.register(NodeRegistration.of("test", of(ObjectiveCppSourceSetExtensible.class)));
		assertDoesNotThrow(() -> modelRegistry.get("test.objectiveCpp", ObjectiveCppSourceSet.class));
	}
}
