/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.uptodate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.runnerkit.BuildTask;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.UpToDateCheck;
import dev.nokee.buildadapter.xcode.PBXProjectTestUtils;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildConfigurationList;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.childNameOrPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mainGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.matching;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.productsGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetName;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targets;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.skipped;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
public abstract class UpToDateCheckSpec {
	GradleRunner executer;
	@TestDirectory Path testDirectory;

	protected static UnaryOperator<PBXProject> alternateBuiltProduct(String filename) {
		return project -> {
			PBXProject result = project;
			val productsGroup = (PBXGroup) project.getMainGroup().getChildren().stream().filter(childNameOrPath("Products")).collect(onlyElement());
			val sourceFile = (PBXFileReference) productsGroup.getChildren().stream().filter(childNameOrPath(filename)).collect(onlyElement());

			val builder = PBXFileReference.builder().sourceTree(PBXSourceTree.of("CONFIGURATION_BUILD_DIR"));
			sourceFile.getPath().ifPresent(builder::path);
			builder.name("alternate-" + filename);
			val alternateFile = builder.build();

			result = productsGroup(children(add(alternateFile))).apply(result);
			return result;
		};
	}

	static PBXBuildPhase aBuildPhase() {
		return PBXCopyFilesBuildPhase.builder().destination(it -> it.resources("")).build();
	}

	protected UnaryOperator<PBXProject> alternateFileUnderTest(String filename) {
		return project -> {
			PBXProject result = project;
			val groupUnderTest = (PBXGroup) project.getMainGroup().getChildren().stream().filter(childNameOrPath(targetUnderTestName())).collect(onlyElement());
			val sourceFile = (PBXFileReference) groupUnderTest.getChildren().stream().filter(childNameOrPath(filename)).collect(onlyElement());

			val builder = PBXFileReference.builder().sourceTree(PBXSourceTree.of("SOURCE_ROOT"));
			builder.path(targetUnderTestName() + "/" + filename);
			builder.name("alternate-" + filename);
			val alternateFile = builder.build();

			result = groupUnderTest(children(add(alternateFile))).apply(result);
			return result;
		};
	}

	protected static BiFunction<PBXProject, XCConfigurationList, XCConfigurationList> debugBuildConfiguration(BiFunction<? super PBXProject, ? super XCBuildConfiguration, ? extends XCBuildConfiguration> action) {
		return (self, buildConfigurations) -> {
			val newBuildConfigurations = PBXProjectTestUtils.<PBXProject, XCBuildConfiguration>matching((XCBuildConfiguration it) -> it.getName().equals("Debug"), action).apply(self, buildConfigurations.getBuildConfigurations());
			return buildConfigurations.toBuilder().buildConfigurations(newBuildConfigurations).build();
		};
	}

	protected static BiFunction<PBXProject, XCConfigurationList, XCConfigurationList> releaseBuildConfiguration(BiFunction<? super PBXProject, ? super XCBuildConfiguration, ? extends XCBuildConfiguration> action) {
		return (self, buildConfigurations) -> {
			val newBuildConfigurations = PBXProjectTestUtils.<PBXProject, XCBuildConfiguration>matching((XCBuildConfiguration it) -> it.getName().equals("Release"), action).apply(self, buildConfigurations.getBuildConfigurations());
			return buildConfigurations.toBuilder().buildConfigurations(newBuildConfigurations).build();
		};
	}

	protected static <T> BiFunction<PBXProject, List<T>, List<T>> shuffleOrdering() {
		return (self, values) -> {
			assert values.size() > 1;
			val result = ImmutableList.<T>builder().add(values.get(values.size() - 1)).addAll(values.subList(0, values.size() - 1)).build();
			assert result.size() == values.size();
			return result;
		};
	}

	protected static Function<PBXBuildFile, PBXBuildFile> changeSettings() {
		return buildFile -> buildFile.toBuilder() //
			.settings(ImmutableMap.<String, Object>builder().putAll(buildFile.getSettings()).put("foo", "FOO").build()) //
			.build();
	}

	protected static BiFunction<PBXProject, XCBuildConfiguration, XCBuildConfiguration> changeBuildSettings() {
		return (self, buildConfiguration) -> buildConfiguration.toBuilder() //
			.buildSettings(buildConfiguration.getBuildSettings().toBuilder().put("foo", "FOO").build()) //
			.build();
	}

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		new UpToDateCheck().writeToProject(testDirectory);

		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withTasks(targetUnderTestName() + "Debug").withArgument("-Dsdk=macosx");
	}

	protected String targetUnderTestName() {
		return "ComponentUnderTest";
	}

	protected final BuildTask targetUnderTestExecution() {
		return executer.build().task(":UpToDateCheck:" + targetUnderTestName() + "Debug");
	}

	static void appendMeaningfulChangeToCFile(Path location) throws IOException {
		assert location.getFileName().toString().endsWith(".c");
		// Need to make a meaningful change so the final binary changes (this change will add a .data entry)
		Files.write(location, Arrays.asList("int value = 42;"), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	static void appendChangeToCHeader(Path location) throws IOException {
		assert location.getFileName().toString().endsWith(".h");
		Files.write(location, Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	static void appendChangeToSwiftFile(Path location) throws IOException {
		assert location.getFileName().toString().endsWith(".swift");
		Files.write(location, Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}

	static void writeColorSet(Path location) throws IOException {
		Files.createDirectories(location);
		Files.write(location.resolve("Contents.json"), Arrays.asList("{\n" +
			"  \"colors\" : [\n" +
			"    {\n" +
			"      \"idiom\" : \"universal\"\n" +
			"    }\n" +
			"  ],\n" +
			"  \"info\" : {\n" +
			"    \"author\" : \"xcode\",\n" +
			"    \"version\" : 1\n" +
			"  }\n" +
			"}"), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
	}

	static void ensureUpToDate(GradleRunner executer) {
		executer.build();
		assertThat(executer.build().getTasks(), everyItem(skipped()));
	}

	Path appDebugProductsDirectory() {
		return buildDirectory().resolve("derivedData/App/Build/Products/Debug");
	}

	Path buildDirectory() {
		return testDirectory.resolve("build/subprojects/UpToDateCheck-1jnf0zhg14ui3");
	}

	protected static <T> Function<T, T> run(Executable executable) {
		return it -> {
			try {
				executable.execute();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			return it;
		};
	}

	protected void xcodeproj(Function<PBXProject, PBXProject> action) {
		mutateProject(action).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
	}

	protected UnaryOperator<PBXProject> targetUnderTest(BiFunction<? super PBXProject, ? super PBXTarget, ? extends PBXTarget> action) {
		return targets(matching(targetName(targetUnderTestName()), action));
	}

	protected UnaryOperator<PBXProject> groupUnderTest(BiFunction<? super PBXProject, ? super PBXGroup, ? extends PBXGroup> action) {
		return mainGroup(children(matching(childNameOrPath(targetUnderTestName()), asGroup(action))));
	}

	protected Function<PBXProject, PBXBuildFile> buildFileTo(String name) {
		return self -> {
			val appGroup = (PBXGroup) self.getMainGroup().getChildren().stream().filter(childNameOrPath(targetUnderTestName())).collect(onlyElement());
			val fileRef = (PBXFileReference) appGroup.getChildren().stream().filter(childNameOrPath(name)).collect(onlyElement());
			return PBXBuildFile.builder().fileRef(fileRef).build();
		};
	}

	protected Function<PBXProject, PBXBuildFile> buildFileTo(String name, Consumer<? super PBXBuildFile.Builder> action) {
		return self -> {
			val appGroup = (PBXGroup) self.getMainGroup().getChildren().stream().filter(childNameOrPath(targetUnderTestName())).collect(onlyElement());
			val fileRef = (PBXFileReference) appGroup.getChildren().stream().filter(childNameOrPath(name)).collect(onlyElement());
			val builder = PBXBuildFile.builder();
			action.accept(builder);
			return builder.fileRef(fileRef).build();
		};
	}

	protected final Path file(String path) {
		return testDirectory.resolve(path);
	}

	protected abstract class BuildConfigurationsTester {
		@Test
		void outOfDateWhenBuildSettingsOfRelatedBuildConfigurationChanges() {
			// type/ordering changes will result in a simple change to the build settings because it's essentially a map
			xcodeproj(targetUnderTest(buildConfigurationList(debugBuildConfiguration(changeBuildSettings()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void ignoreChangesToBuildSettingsOfUnrelatedBuildConfiguration() {
			xcodeproj(targetUnderTest(buildConfigurationList(releaseBuildConfiguration(changeBuildSettings()))));

			assertThat(targetUnderTestExecution(), upToDate());
		}
	}
}
