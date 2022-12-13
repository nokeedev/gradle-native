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
package dev.nokee.buildadapter.xcode;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.internal.testing.junit.jupiter.GradleFeatureRequirement;
import dev.nokee.internal.testing.junit.jupiter.RequiresGradleFeature;
import dev.nokee.platform.xcode.XcodeSwiftApp;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.ProductTypes;
import dev.nokee.xcode.project.PBXObjectArchiver;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProjReader;
import dev.nokee.xcode.project.PBXProjWriter;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.doSomethingVerifyTask;
import static dev.nokee.internal.testing.GradleConfigurationCacheMatchers.configurationCache;
import static dev.nokee.internal.testing.GradleConfigurationCacheMatchers.recalculated;
import static dev.nokee.internal.testing.GradleConfigurationCacheMatchers.reused;
import static dev.nokee.xcode.utils.PropertyListTestUtils.writeXmlPlistTo;
import static dev.nokee.xcode.utils.XCWorkspaceDataTestUtils.emptyWorkspaceData;
import static dev.nokee.xcode.utils.XCWorkspaceDataTestUtils.writeTo;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationCacheDetectsXcodeProjectChangesFunctionalTests {
	GradleRunner executer;
	@TestDirectory Path testDirectory;
	BuildResult result;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		new XcodeSwiftApp().writeToProject(testDirectory.toFile());

		doSomethingVerifyTask().writeTo(testDirectory.resolve("build.gradle"));
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withArgument("verify").withArgument("--configuration-cache");
		result = executer.build();
	}

	@Nested
	class WhenNoChanges {
		@Test
		void reuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenProjectPbxprojChangesInNonMeaningfulWay {
		@BeforeEach
		void givenPbxprojWithExtraLine() throws IOException {
			// We serialize the project model hence, any change that doesn't change the model will be no-op.
			Files.write(testDirectory.resolve("XcodeSwiftApp.xcodeproj/project.pbxproj"), Collections.singletonList(""), StandardOpenOption.APPEND);
		}

		@Test
		void reuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenEmbeddedWorkspaceContentChanges {
		@BeforeEach
		void givenNewWorkspaceContent() throws IOException {
			writeTo(emptyWorkspaceData(), testDirectory.resolve("XcodeSwiftApp.xcodeproj/project.xcworkspace/contents.xcworkspacedata"));
		}

		@Test
		void ignoresChangesAndReuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenIDEWorkspaceChecksChanges {
		@BeforeEach
		void givenNewIDEWorkspaceChecks() throws IOException {
			writeXmlPlistTo(emptyMap(), testDirectory.resolve("XcodeSwiftApp.xcodeproj/project.xcworkspace/xcshareddata/IDEWorkspaceChecks.plist"));
		}

		@Test
		void ignoresChangesAndReuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenTargetRemoved {
		@BeforeEach
		void givenTargetRemoved() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), targets(remove(it -> it.getName().equals("XcodeSwiftAppUITests"))));
		}

		@Test
		void doesNotReuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(recalculated()));
		}
	}

	@Nested
	class WhenTargetAdded {
		@BeforeEach
		void givenTargetAdded() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), targets(add(project -> PBXAggregateTarget.builder().name("Foo").buildConfigurations(XCConfigurationList.builder().lenient().buildConfigurations(project.getBuildConfigurationList().getBuildConfigurations().stream().map(it -> XCBuildConfiguration.builder().name(it.getName()).build()).collect(Collectors.toList())).build()).build())));
		}

		@Test
		void doesNotReuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(recalculated()));
		}
	}

	@Nested
	class WhenTargetOrderingChanges {
		@BeforeEach
		void givenTargetOrderingChanged() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), targets(rotate(1)));
		}

		@Test
		void reuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenTargetDetailsChangesExceptForName {
		@BeforeEach
		void givenTargetReplaced() throws IOException {
			// We only care about the target name during configuration
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), targets(replace(it -> it.getName().equals("XcodeSwiftApp"), withDifferentTargetButSameName())));
		}

		@Test
		void reuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenProjectConfigurationAdded {
		@BeforeEach
		void givenConfigurationAdded() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), buildConfigurationList(buildConfigurations(add(it -> XCBuildConfiguration.builder().name("DebugOptimized").build()))));
		}

		@Test
		void doesNotReuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(recalculated()));
		}
	}

	@Nested
	class WhenProjectConfigurationRemoved {
		@BeforeEach
		void givenConfigurationRemoved() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), buildConfigurationList(buildConfigurations(remove(it -> it.getName().equals("Debug")))));
		}

		@Test
		void doesNotReuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(recalculated()));
		}
	}

	@Nested
	class WhenConfigurationOrderingChanges {
		@BeforeEach
		void givenConfigurationOrderingChanged() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), buildConfigurationList(buildConfigurations(rotate(1))));
		}

		@Test
		void reuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	@Nested
	class WhenConfigurationDetailsChangesExceptForName {
		@BeforeEach
		void givenConfigurationReplaced() throws IOException {
			project(testDirectory.resolve("XcodeSwiftApp.xcodeproj"), buildConfigurationList(buildConfigurations(replace(it -> it.getName().equals("Release"), withDifferentConfigurationButSameName()))));
		}

		@Test
		void reuseConfigurationCache() {
			assertThat(executer.build(), configurationCache(reused()));
		}
	}

	private static void project(Path path, Function<? super PBXProject, ? extends PBXProject> mapper) throws IOException {
		PBXProject project = null;
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(path.resolve("project.pbxproj"), StandardCharsets.UTF_8)))) {
			project = new PBXObjectUnarchiver().decode(reader.read());
		}

		try (val writer = new PBXProjWriter(Files.newBufferedWriter(path.resolve("project.pbxproj"), StandardCharsets.UTF_8))) {
			writer.write(new PBXObjectArchiver().encode(mapper.apply(project)));
		}
	}

	private static Function<PBXProject, PBXProject> targets(BiFunction<? super PBXProject, ? super List<PBXTarget>, ? extends List<PBXTarget>> mapper) {
		return project -> project.toBuilder().targets(mapper.apply(project, project.getTargets())).build();
	}

	private static Function<PBXProject, PBXProject> buildConfigurationList(BiFunction<? super PBXProject, ? super XCConfigurationList, ? extends XCConfigurationList> mapper) {
		return project -> project.toBuilder().buildConfigurations(mapper.apply(project, project.getBuildConfigurationList())).build();
	}

	private static BiFunction<PBXProject, XCConfigurationList, XCConfigurationList> buildConfigurations(BiFunction<? super PBXProject, ? super List<XCBuildConfiguration>, ? extends List<XCBuildConfiguration>> mapper) {
		return (project, configurationList) -> {
			return configurationList.toBuilder().buildConfigurations(mapper.apply(project, configurationList.getBuildConfigurations())).build();
		};
	}

	private static <T, U> BiFunction<T, List<U>, List<U>> add(Function<? super T, ? extends U> function) {
		return (self, target) -> ImmutableList.<U>builder().addAll(target).add(function.apply(self)).build();
	}

	private static <T, U> BiFunction<T, List<U>, List<U>> remove(Predicate<? super U> predicate) {
		return (self, allItems) -> allItems.stream().filter(predicate).collect(ImmutableList.toImmutableList());
	}

	private static <T, U> BiFunction<T, List<U>, List<U>> rotate(int distance) {
		return (self, allItems) -> {
			List<U> result = new ArrayList<>(allItems);
			Collections.rotate(result, distance);
			return ImmutableList.copyOf(result);
		};
	}

	private static BiFunction<PBXProject, PBXTarget, PBXTarget> withDifferentTargetButSameName() {
		return (self, item) -> {
			return PBXNativeTarget.builder().name(item.getName()).productName("SomeOtherName").productReference(PBXFileReference.ofAbsolutePath("/some/path")).productType(ProductTypes.FRAMEWORK).buildConfigurations(XCConfigurationList.builder().lenient().build()).build();
		};
	}

	private static <T, U> BiFunction<T, List<U>, List<U>> replace(Predicate<? super U> predicate, BiFunction<? super T, ? super U, ? extends U> mapper) {
		return (self, allItems) -> {
			assertTrue(allItems.stream().anyMatch(predicate));
			return allItems.stream().map(item -> {
				if (predicate.test(item)) {
					return mapper.apply(self, item);
				} else {
					return item;
				}
			}).collect(ImmutableList.toImmutableList());
		};
	}

	private static BiFunction<PBXProject, XCBuildConfiguration, XCBuildConfiguration> withDifferentConfigurationButSameName() {
		return (self, item) -> XCBuildConfiguration.builder().name(item.getName()).build();
	}
}
