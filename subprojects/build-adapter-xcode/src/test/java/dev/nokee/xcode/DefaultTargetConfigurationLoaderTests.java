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
package dev.nokee.xcode;

import dev.nokee.buildadapter.xcode.TestProjectReference;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.project.DefaultKeyedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DefaultTargetConfigurationLoaderTests {
	XCTargetReference reference = project("MyProject").ofTarget("Test");

	static abstract class GivenPBXProjectLoading {
		@Mock XCLoader<PBXProject, XCProjectReference> loader;
		@InjectMocks DefaultTargetConfigurationLoader subject;

		@BeforeEach
		void givenPBXProjectLoading() {
			Mockito.when(loader.load(TestProjectReference.project("MyProject"))).thenReturn(project());
		}

		public abstract PBXProject project();
	}

	private static PBXTarget newTarget(String name) {
		return PBXAggregateTarget.builder().lenient().name(name).build();
	}

	private static PBXTarget newTarget(String name, Consumer<? super XCConfigurationList.Builder> action) {
		return PBXAggregateTarget.builder().name(name).buildConfigurations(action).build();
	}

	private static Consumer<XCConfigurationList.Builder> withoutDefaultBuildConfiguration() {
		return builder -> builder.buildConfiguration(it -> it.name("Debug")).buildConfiguration(it -> it.name("Release"));
	}

	private static Consumer<XCConfigurationList.Builder> withDefaultBuildConfiguration(String defaultBuildConfigurationName) {
		return builder -> builder.buildConfiguration(it -> it.name("Debug")).buildConfiguration(it -> it.name("Release")).defaultConfigurationName(defaultBuildConfigurationName);
	}

	private static UnaryOperator<PBXProject.Builder> targets() {
		return builder -> builder.target(newTarget("Foo")) //
			.target(newTarget("Bar")) //
			.target(newTarget("Far"));
	}

	private static UnaryOperator<PBXProject.Builder> noTargets() {
		return builder -> builder.targets(Collections.emptyList());
	}


	private static UnaryOperator<PBXProject.Builder> testTargetWithoutBuildConfigurations() {
		return builder -> builder.target(newTarget("Foo")) //
			.target(newTarget("Test")) //
			.target(newTarget("Far"));
	}

	private static UnaryOperator<PBXProject.Builder> testTarget(Consumer<? super XCConfigurationList.Builder> action) {
		return builder -> builder.target(newTarget("Foo")) //
			.target(newTarget("Test", action)) //
			.target(newTarget("Far"));
	}

	@Nested
	class WhenProjectWithoutTargets extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.with(noTargets()) //
				.build();
		}

		@Test
		void throwsExceptionBecauseReferenceIsInvalid() {
			assertThrows(RuntimeException.class, () -> subject.load(reference));
		}
	}

	@Nested
	class WhenProjectWithoutTargetsButNoneMatchTargetReference extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.with(targets()) //
				.build();
		}

		@Test
		void throwsExceptionBecauseReferenceIsInvalid() {
			assertThrows(RuntimeException.class, () -> subject.load(reference));
		}
	}


	//region: PBXProject without build configurations
	@Nested
	class WhenProjectWithoutBuildConfigurationsHasTestTargetWithoutBuildConfigurations extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder()
				.with(testTargetWithoutBuildConfigurations())
				.build();
		}

		@Test
		void returnNullBecauseNeitherTargetOrProjectHaveBuildConfigurations() {
			assertThat(subject.load(reference), nullValue());
		}
	}

	@Nested
	class WhenProjectWithoutBuildConfigurationsHasTestTargetWithoutDefaultBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.with(testTarget(withoutDefaultBuildConfiguration())) //
				.build();
		}

		@Test
		void returnNullBecauseTargetDoesNotHaveDefaultBuildConfigurationAndProjectHasNoBuildConfigurations() {
			assertThat(subject.load(reference), nullValue());
		}
	}

	@Nested
	class WhenProjectWithoutBuildConfigurationsHasTestTargetWithDefaultBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.with(testTarget(withDefaultBuildConfiguration("Release"))) //
				.build();
		}

		@Test
		void returnsDefaultBuildConfigurationOfTarget() {
			assertThat(subject.load(reference), equalTo("Release"));
		}
	}
	//endregion

	//region: PBXProject without default build configuration
	@Nested
	class WhenProjectWithoutDefaultBuildConfigurationHasTestTargetWithoutBuildConfigurations extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.buildConfigurations(withoutDefaultBuildConfiguration()) //
				.with(testTargetWithoutBuildConfigurations()) //
				.build();
		}

		@Test
		void returnNullBecauseProjectConfigurationDoesNotHaveDefaultBuildConfiguration() {
			assertThat(subject.load(reference), nullValue());
		}
	}

	@Nested
	class WhenProjectWithoutDefaultBuildConfigurationHasTestTargetWithoutDefaultBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.buildConfigurations(withoutDefaultBuildConfiguration()) //
				.with(testTarget(withoutDefaultBuildConfiguration())) //
				.build();
		}

		@Test
		void returnsNullBecauseNeitherTargetOrProjectHasDefaultBuildConfiguration() {
			assertThat(subject.load(reference), nullValue());
		}
	}

	@Nested
	class WhenProjectWithoutDefaultBuildConfigurationHasTestTargetWithDefaultBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.buildConfigurations(withoutDefaultBuildConfiguration()) //
				.with(testTarget(withDefaultBuildConfiguration("Release"))) //
				.build();
		}

		@Test
		void returnsDefaultBuildConfigurationOfTarget() {
			assertThat(subject.load(reference), equalTo("Release"));
		}
	}
	//endregion

	//region: PBXProject with default configuration (debug)
	@Nested
	class WhenProjectWithDefaultBuildConfigurationHasTestTargetWithoutBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.buildConfigurations(withDefaultBuildConfiguration("Debug")) //
				.with(testTargetWithoutBuildConfigurations()) //
				.build();
		}

		@Test
		void returnsProjectDefaultBuildConfigurationBecauseTargetHasNoBuildConfigurations() {
			assertThat(subject.load(reference), equalTo("Debug"));
		}
	}

	@Nested
	class WhenProjectWithDefaultBuildConfigurationHasTestTargetWithoutDefaultBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.buildConfigurations(withDefaultBuildConfiguration("Debug")) //
				.with(testTarget(withoutDefaultBuildConfiguration())) //
				.build();
		}

		@Test
		void returnsProjectDefaultBuildConfigurationBecauseTargetDoesNotHaveDefaultBuildConfiguration() {
			assertThat(subject.load(reference), equalTo("Debug"));
		}
	}

	@Nested
	class WhenProjectWithDefaultBuildConfigurationHasTestTargetWithDefaultBuildConfiguration extends GivenPBXProjectLoading {
		public PBXProject project() {
			return PBXProject.builder() //
				.buildConfigurations(withDefaultBuildConfiguration("Debug")) //
				.with(testTarget(withDefaultBuildConfiguration("Release"))) //
				.build();
		}

		@Test
		void returnsProjectDefaultBuildConfigurationBecauseTargetDoesNotHaveDefaultBuildConfiguration() {
			assertThat(subject.load(reference), equalTo("Release"));
		}
	}
	//endregion
}
