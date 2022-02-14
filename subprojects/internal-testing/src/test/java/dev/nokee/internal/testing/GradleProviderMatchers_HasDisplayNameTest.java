/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.internal.testing;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.hasDisplayName;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;

class GradleProviderMatchers_HasDisplayNameTest extends AbstractMatcherTest {
	private static final PropertiesWithDisplayName PROPERTIES = objectFactory().newInstance(PropertiesWithDisplayName.class);

	@Override
	protected Matcher<?> createMatcher() {
		return hasDisplayName("some display name");
	}

	@Test
	void matchesCustomPropertyDisplayName() {
		assertMatches(hasDisplayName("property 'myProperty'"), PROPERTIES.getMyProperty(),
			"matches property display name");
	}

	@Test
	void matchesCustomSetPropertyDisplayName() {
		assertMatches(hasDisplayName("property 'mySetProperty'"), PROPERTIES.getMySetProperty(),
			"matches set property display name");
	}

	@Test
	void matchesCustomListPropertyDisplayName() {
		assertMatches(hasDisplayName("property 'myListProperty'"), PROPERTIES.getMyListProperty(),
			"matches list property display name");
	}

	@Test
	void matchesCustomFilePropertyDisplayName() {
		assertMatches(hasDisplayName("property 'myFileProperty'"), PROPERTIES.getMyFileProperty(),
			"matches file property display name");
	}

	@Test
	void matchesCustomDirectoryPropertyDisplayName() {
		assertMatches(hasDisplayName("property 'myDirectoryProperty'"), PROPERTIES.getMyDirectoryProperty(),
			"matches directory property display name");
	}

	@Test
	void matchesCustomMapPropertyDisplayName() {
		assertMatches(hasDisplayName("property 'myMapProperty'"), PROPERTIES.getMyMapProperty(),
			"matches map property display name");
	}

	@Test
	void doesNotMatchDefaultPropertyDisplayNameOnCustomProperty() {
		assertDoesNotMatch(hasDisplayName("this property"), PROPERTIES.getMyProperty(),
			"does not match default display name");
	}

	@Test
	void matchesDefaultPropertyDisplayName() {
		assertMatches(hasDisplayName("this property"), objectFactory().property(String.class),
			"matches default property display name");
	}

	public interface PropertiesWithDisplayName {
		Property<String> getMyProperty();
		SetProperty<String> getMySetProperty();
		ListProperty<String> getMyListProperty();
		DirectoryProperty getMyDirectoryProperty();
		RegularFileProperty getMyFileProperty();
		MapProperty<String, String> getMyMapProperty();
	}
}
