/*
 * Copyright 2021 the original author or authors.
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

import dev.nokee.language.nativebase.NativeLanguageSourceSetIntegrationTester;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;

public abstract class ObjectiveCSourceSetIntegrationTester extends NativeLanguageSourceSetIntegrationTester<ObjectiveCSourceSet> {
	@Nested
	class ObjectiveCCompileTaskTest implements ObjectiveCCompileTester {
		@Override
		public ObjectiveCCompileTask subject() {
			return (ObjectiveCCompileTask) project().getTasks().getByName("compile" + StringUtils.capitalize(variantName()));
		}
	}
}
