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

import com.google.gson.Gson;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.platform.xcode.XcodeSwiftApp;
import dev.nokee.samples.xcode.GreeterAppWithRemoteLib;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class XcodeProjectInspectionFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void appliesPluginUnderTest(GradleRunner runner) throws IOException {
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		this.executer = runner.withArgument("--quiet");
	}

	private static String[] inspect(String projectPath) {
		return new String[] { projectPath + ":inspect", "--format=json" };
	}

	@Test
	void canInspectXcodeProject() {
		new XcodeSwiftApp().writeToProject(testDirectory);
		val report = XCProjectInspectionReport.from(executer.withTasks(inspect(":XcodeSwiftApp")).build().getOutput());
		assertThat(report.getName(), equalTo("XcodeSwiftApp"));
		assertThat(report.getTargets(), contains("XcodeSwiftApp", "XcodeSwiftAppTests", "XcodeSwiftAppUITests"));
	}

	@Test
	void canInspectXcodeTarget() {
		new XcodeSwiftApp().writeToProject(testDirectory);
		assertAll(() -> {
				val report = XCTargetInspectionReport.from(executer.withTasks(inspect("XcodeSwiftApp")).withTasks("--target=XcodeSwiftApp").build().getOutput());
				assertThat(report.getName(), equalTo("XcodeSwiftApp"));
				assertThat(report.getDependencies(), emptyIterable());
				assertThat(report.getInputFiles(), contains("$(SOURCE_ROOT)/XcodeSwiftApp/ViewController.swift", "$(SOURCE_ROOT)/XcodeSwiftApp/AppDelegate.swift", "$(SOURCE_ROOT)/XcodeSwiftApp/Assets.xcassets"));
				assertThat(report.getProduct().getLocation(), equalTo("$(BUILT_PRODUCT_DIR)/XcodeSwiftApp.app"));
			},
			() -> {
				val report = XCTargetInspectionReport.from(executer.withTasks(inspect("XcodeSwiftApp")).withTasks("--target=XcodeSwiftAppTests").build().getOutput());
				assertThat(report.getName(), equalTo("XcodeSwiftAppTests"));
				assertThat(report.getDependencies(), contains("XcodeSwiftApp:XcodeSwiftApp (local)"));
				assertThat(report.getInputFiles(), contains("$(SOURCE_ROOT)/XcodeSwiftAppTests/XcodeSwiftAppTests.swift"));
				assertThat(report.getProduct().getLocation(), equalTo("$(BUILT_PRODUCT_DIR)/XcodeSwiftAppTests.xctest"));
			},
			() -> {
				val report = XCTargetInspectionReport.from(executer.withTasks(inspect("XcodeSwiftApp")).withTasks("--target=XcodeSwiftAppUITests").build().getOutput());
				assertThat(report.getName(), equalTo("XcodeSwiftAppUITests"));
				assertThat(report.getDependencies(), contains("XcodeSwiftApp:XcodeSwiftApp (local)"));
				assertThat(report.getInputFiles(), contains("$(SOURCE_ROOT)/XcodeSwiftAppUITests/XcodeSwiftAppUITestsLaunchTests.swift", "$(SOURCE_ROOT)/XcodeSwiftAppUITests/XcodeSwiftAppUITests.swift"));
				assertThat(report.getProduct().getLocation(), equalTo("$(BUILT_PRODUCT_DIR)/XcodeSwiftAppUITests.xctest"));
			});
	}

	@Test
	void canInspectXcodeTargetWithCrossProjectReference() {
		new GreeterAppWithRemoteLib().writeToProject(testDirectory);
		val report = XCTargetInspectionReport.from(executer.withTasks(inspect("GreeterApp")).withTasks("--target=GreeterApp").build().getOutput());
		assertThat(report.getName(), equalTo("GreeterApp"));
		assertThat(report.getDependencies(), contains("GreeterLib:GreeterLib (remote)"));
		assertThat(report.getInputFiles(), contains("$(SOURCE_ROOT)/GreeterApp/main.c"));
		assertThat(report.getProduct().getLocation(), equalTo("$(BUILT_PRODUCT_DIR)/GreeterApp"));
	}

	public static final class XCProjectInspectionReport {
		private String name;
		private List<String> targets;

		public String getName() {
			return name;
		}

		public List<String> getTargets() {
			return targets;
		}

		public static XCProjectInspectionReport from(String output) {
			return new Gson().fromJson(output, XCProjectInspectionReport.class);
		}
	}

	public static final class XCTargetInspectionReport {
		private String name;
		private List<String> dependencies;
		private List<String> inputFiles;
		private ProductInspectionReport product;

		public String getName() {
			return name;
		}

		public List<String> getDependencies() {
			return dependencies;
		}

		public List<String> getInputFiles() {
			return inputFiles;
		}

		public ProductInspectionReport getProduct() {
			return product;
		}

		public static final class ProductInspectionReport {
			private String location;

			public String getLocation() {
				return location;
			}
		}

		public static XCTargetInspectionReport from(String output) {
			return new Gson().fromJson(output, XCTargetInspectionReport.class);
		}
	}
}
