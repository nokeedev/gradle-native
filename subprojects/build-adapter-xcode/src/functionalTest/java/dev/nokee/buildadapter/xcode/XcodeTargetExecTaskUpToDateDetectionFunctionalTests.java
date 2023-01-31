///*
// * Copyright 2022 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package dev.nokee.buildadapter.xcode;
//
//import dev.gradleplugins.buildscript.blocks.DependencyNotation;
//import dev.gradleplugins.buildscript.statements.Statement;
//import dev.gradleplugins.buildscript.syntax.Syntax;
//import dev.gradleplugins.runnerkit.BuildResult;
//import dev.gradleplugins.runnerkit.GradleExecutionResult;
//import dev.gradleplugins.runnerkit.GradleRunner;
//import dev.nokee.buildadapter.xcode.internal.plugins.CurrentXcodeInstallationValueSource;
//import dev.nokee.buildadapter.xcode.internal.plugins.HasConfigurableXcodeInstallation;
//import dev.nokee.buildadapter.xcode.internal.plugins.HasConfigurableXcodeTarget;
//import dev.nokee.buildadapter.xcode.internal.plugins.XCSpecLoader;
//import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
//import dev.nokee.xcode.XCTargetReference;
//import dev.nokee.xcode.objects.PBXProject;
//import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
//import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledOnOs;
//import org.junit.jupiter.api.condition.OS;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.buildscript;
//import static dev.gradleplugins.buildscript.blocks.BuildScriptBlock.classpath;
//import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksExecutedAndNotSkipped;
//import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksSkipped;
//import static org.hamcrest.MatcherAssert.assertThat;
//
//@EnabledOnOs(OS.MAC)
//@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
//class XcodeTargetExecTaskUpToDateDetectionFunctionalTests {
//	GradleRunner executer;
//	@TestDirectory Path testDirectory;
//	BuildResult result;
//
//	// Changes Xcode installation -> (different version but same location) -> out-of-date
//	// Changes Xcode installation -> (different version and location) -> out-of-date
//	// Changes Xcode installation -> (same version and different location) -> out-of-date
//
//	// Extra build phase -> out-of-date
//	// Less build phase -> out-of-date
//	// Build phase changes -> (name change) -> up-to-date
//	// Build phase changes -> (type change - and possibly name change) -> out-of-date
//
//	// Output of one phase and input to another phase -> ignore as input/output files of the task
//
//	// Per build phase type
//	// PBXShellScript -> shell path or shell script change -> out-of-date
//	// PBXShellScript -> input/output (xcfilelist) change but same resolved file -> up-to-date
//
//	// PBXCopyFiles -> SubFolder change -> out-of-date
//	// PBXCopyFiles -> dstFileChange but same resolved file -> up-to-date
//	// PBXCopyFiles -> dstFileChange but different resolved file -> out-of-date
//
//	// PBXBuildFiles -> change build settings -> out-of-date
//	// PBXBuildFiles -> change file ref but same resolved file -> up-to-date
//	// PBXBuildFiles -> change file ref but different resolved file -> out-of-date
//
//	// PBXTarget -> productname change -> out-of-date
//	// PBXTarget -> product ref change but same resolved file -> up-to-date
//	// PBXTarget -> product ref change but different resolved file -> out-of-date
//	// PBXTarget -> explicit dependencies -> handled by derived data
//	// PBXTarget -> name --> handled by build settings
//	// PBXTarget -> product type -> not sure if we should care about this?
//
//	// PBXTarget -> build settings -> changes -> out-of-date
//
//	@BeforeEach
//	void givenCustomTaskWithCurrentXcodeInstallationConfigured(GradleRunner runner) throws IOException {
//		buildscript(it -> it.dependencies(classpath(DependencyNotation.files(runner.getPluginClasspath())))).writeTo(testDirectory.resolve("settings.gradle"));
//		Statement.expressionOf(Syntax.groovy(Stream.of("",
//			"import " + HasConfigurableXcodeTarget.class.getCanonicalName(),
//			"import " + XCSpecLoader.class.getCanonicalName(),
//			"import " + XCTargetReference.class.getCanonicalName(),
//			"",
//			"abstract class MyTask extends DefaultTask implements " + HasConfigurableXcodeTarget.class.getSimpleName() + " {",
//			"    @OutputFile",
//			"    abstract RegularFileProperty getOutputFile()",
//			"    @TaskAction",
//			"    void execute() {",
//			"        outputFile.get().asFile.text = 'some data'",
//			"    }",
//			"}",
//			"",
//			"tasks.register('verify', MyTask) {",
//			"    outputFile = project.file('out.txt')",
//			"    target = XCProjectReference.of(file('MyProject.xcodeproj')).ofTarget('MyTarget')",
//			"    targetSpec = target.map { it.load(new XCSpecLoader()) }",
//			"}"
//		).collect(Collectors.joining("\n")))).writeTo(testDirectory.resolve("build.gradle"));
//		executer = runner.withArgument("verify");
//	}
//
//	@Nested
//	class WhenUpToDateBuild {
//		@BeforeEach
//		void givenBuildIsUpToDate() {
//			PBXProject.builder().target()
//			assertThat(result = executer.build(), tasksExecutedAndNotSkipped(":verify"));
//			assertThat(result = executer.build(), tasksSkipped(":verify"));
//		}
//
//		@Test
//		void dfd() {
//			result.task()
//		}
//	}
//}
