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
package dev.nokee.platform.base.testers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isA;

public abstract class DependencyUnderTest {
	private final String displayName;
	private final Object notation;

	private DependencyUnderTest(String displayName, Object notation) {
		this.displayName = displayName;
		this.notation = notation;
	}

	public Object asNotation() {
		return notation;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public abstract Matcher<Dependency> asMatcher();


	public static DependencyUnderTest projectDependency(@Nullable String group, String name, @Nullable String version) {
		Project notation = ProjectBuilder.builder().withName(name).build();
		Optional.ofNullable(group).ifPresent(notation::setGroup);
		Optional.ofNullable(version).ifPresent(notation::setVersion);
		return new DependencyUnderTest("project dependency '" + name + "'", notation) {
			@Override
			public Matcher<Dependency> asMatcher() {
				return allOf(isA(ProjectDependency.class), forCoordinate(group, name, version));
			}
		};
	}

	public static ExternalDependencyUnderTest externalDependency(@Nullable String group, String name, @Nullable String version) {
		return new ExternalDependencyUnderTest() {
			@Override
			public DependencyUnderTest declaredAsMap() {
				ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();
				Optional.ofNullable(group).ifPresent(it -> builder.put("group", it));
				builder.put("name", name);
				Optional.ofNullable(version).ifPresent(it -> builder.put("version", it));
				Map<String, String> notation = builder.build();
				return new DependencyUnderTest("external dependency '" + notation + "'", notation) {
					@Override
					public Matcher<Dependency> asMatcher() {
						return allOf(isA(ExternalModuleDependency.class), forCoordinate(group, name, version));
					}
				};
			}

			@Override
			public DependencyUnderTest declaredAsString() {
				StringBuilder builder = new StringBuilder();
				Optional.ofNullable(group).ifPresent(builder::append);
				builder.append(":").append(name);
				Optional.ofNullable(version).ifPresent(it -> builder.append(":").append(it));
				String notation = builder.toString();
				return new DependencyUnderTest("external dependency '" + notation + "'", notation) {
					@Override
					public Matcher<Dependency> asMatcher() {
						return allOf(isA(ExternalModuleDependency.class), forCoordinate(group, name, version));
					}
				};
			}
		};
	}

	public interface ExternalDependencyUnderTest {
		DependencyUnderTest declaredAsMap();

		DependencyUnderTest declaredAsString();

		default Stream<DependencyUnderTest> allDeclaration() {
			return Stream.of(declaredAsMap(), declaredAsString());
		}
	}

	public static DependencyUnderTest filesDependency(File... files) {
		return new DependencyUnderTest("files dependencies [" + Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.joining(", ")) + "]", objectFactory().fileCollection().from(files)) {
			@Override
			public Matcher<Dependency> asMatcher() {
				return allOf(isA(FileCollectionDependency.class), new FeatureMatcher<Dependency, Set<File>>(containsInAnyOrder(files), "", "") {
					@Override
					protected Set<File> featureValueOf(Dependency actual) {
						return ((FileCollectionDependency) actual).getFiles().getFiles();
					}
				});
			}
		};
	}

	public static Set<DependencyUnderTest> dependencyNotations() {
		return ImmutableSet.of(
			projectDependency("com.example.foo", "foo", "1.0"),
			externalDependency("com.example.bar", "bar", "2.0").declaredAsMap(),
			externalDependency("com.example.far", "far", "3.0").declaredAsString()
		);
	}
}
