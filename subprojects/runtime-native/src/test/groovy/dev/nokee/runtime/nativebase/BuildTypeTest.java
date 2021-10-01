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
package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.BuildType.named;
import static org.hamcrest.MatcherAssert.assertThat;

class BuildTypeTest {
	@Nested
	class ObjectFactoryTest implements NamedValueTester<BuildType> {
		@Override
		public BuildType createSubject(String name) {
			return objectFactory().named(BuildType.class, name);
		}
	}

	@Nested
	class MethodFactoryTest implements NamedValueTester<BuildType> {
		@Override
		public BuildType createSubject(String name) {
			return named(name);
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor("debug"))
			.addEqualityGroup((Object[]) equalityGroupFor("release"))
			.addEqualityGroup((Object[]) equalityGroupFor("RelWithDebug"))
			.testEquals();
	}

	private static BuildType[] equalityGroupFor(String name) {
		return new BuildType[] {
			objectFactory().named(BuildType.class, name),
			named(name),
			objectFactory().named(BuildType.class, name)
		};
	}
}
