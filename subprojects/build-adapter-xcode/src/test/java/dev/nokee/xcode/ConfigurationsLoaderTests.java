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

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ConfigurationsLoaderTests {
	@Mock XCLoader<PBXProject, XCProjectReference> loader;
	@InjectMocks ConfigurationsLoader subject;

	@Nested
	class WhenPBXProjectHasConfigurations {
		Iterable<String> result;
		XCBuildConfiguration debug = XCBuildConfiguration.builder().name("Debug").build();
		XCBuildConfiguration release = XCBuildConfiguration.builder().name("Release").build();

		@BeforeEach
		void givenPBXProjectWithRequestedTarget() {
			Mockito.when(loader.load(any())).thenReturn(PBXProject.builder()
				.buildConfigurations(it -> it.buildConfiguration(debug).buildConfiguration(release)).build());
			result = subject.load(project("MyProject").ofTarget("Bar"));
		}

		@Test
		void returnsProjectConfigurations() {
			assertThat(result, contains("Debug", "Release"));
		}
	}

	@Nested
	class WhenPBXProjectHasNoConfigurations {
		Iterable<String> result;

		@BeforeEach
		void givenPBXProjectWithNoTarget() {
			Mockito.when(loader.load(any())).thenReturn(PBXProject.builder().build());
			result = subject.load(project("MyProject").ofTarget("Foo"));
		}

		@Test
		void returnsEmptyList() {
			assertThat(result, emptyIterable());
		}
	}

	@Nested
	class WhenPBXProjectHasEmptyConfigurationList {
		Iterable<String> result;

		@BeforeEach
		void givenPBXProjectWithTargetsWithoutRequestedTarget() {
			Mockito.when(loader.load(any())).thenReturn(PBXProject.builder().buildConfigurations(XCConfigurationList.builder().build()).build());
			result = subject.load(project("MyProject").ofTarget("Foo"));
		}

		@Test
		void returnsEmptyList() {
			assertThat(result, emptyIterable());
		}
	}
}
