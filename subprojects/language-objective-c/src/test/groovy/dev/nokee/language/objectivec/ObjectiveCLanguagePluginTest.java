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
package dev.nokee.language.objectivec;

import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguagePlugin;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectiveCLanguagePluginTest {
	@Test
	void appliesCppLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCLanguagePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(ObjectiveCLanguageBasePlugin.class), "should apply Objective-C language base plugin");
	}

//	@Test
//	void createsObjectiveCSourceSetUponDiscoveringSourceSetExtensibleType() {
//		val project = rootProject();
//		project.apply(singletonMap("plugin", ObjectiveCLanguagePlugin.class));
//		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
//		modelRegistry.register(NodeRegistration.of("test", of(ObjectiveCSourceSetExtensible.class)));
//		assertDoesNotThrow(() -> modelRegistry.get("test.objectiveC", ObjectiveCSourceSet.class));
//	}
}
