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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.platform.nativebase.internal.dependencies.RequestFrameworkAction;
import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.LibraryElements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.hasAttribute;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(GradleTestExtension.class)
class RequestFrameworkActionTest {
	RequestFrameworkAction subject = new RequestFrameworkAction(objectFactory());

	@Test
	void addsFrameworkLibraryElementsAttribute() {
		ModuleDependency dependency = (ModuleDependency) ProjectTestUtils.createDependency("dev.nokee.framework:foo:1.4.2");
		subject.execute(dependency);
		assertThat(dependency.getAttributes(), hasAttribute(equalTo(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named("framework-bundle")));
	}

	@Test
	void requestsDeserializedArtifactUsingAttribute() {
		ModuleDependency dependency = (ModuleDependency) ProjectTestUtils.createDependency("dev.nokee.framework:foo:1.4.2");
		subject.execute(dependency);
		assertThat(dependency.getAttributes(), hasAttribute(equalTo(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE), equalTo("deserialized")));
	}

	@Test
	void doesNotAddAnyAttributesWhenNotMagicalGroup() {
		ModuleDependency dependency = (ModuleDependency) ProjectTestUtils.createDependency("com.example:foo:1.4.2");
		subject.execute(dependency);
		assertThat(dependency.getAttributes(), attributes(anEmptyMap()));
	}

	@Test
	void doesNotAddAnyAttributesWhenMagicalGroupMatchSubSetOfDependencyGroup__extraSegments() {
		ModuleDependency dependency = (ModuleDependency) ProjectTestUtils.createDependency("dev.nokee.framework.something.else:foo:1.4.2");
		subject.execute(dependency);
		assertThat(dependency.getAttributes(), attributes(anEmptyMap()));
	}

	@Test
	void doesNotAddAnyAttributesWhenMagicalGroupMatchSubSetOfDependencyGroup__frameworkSegmentSubSet() {
		ModuleDependency dependency = (ModuleDependency) ProjectTestUtils.createDependency("dev.nokee.framework-suffix:foo:1.4.2");
		subject.execute(dependency);
		assertThat(dependency.getAttributes(), attributes(anEmptyMap()));
	}
}
