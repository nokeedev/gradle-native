/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.xcode.fixtures;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolProvider;
import dev.nokee.ide.fixtures.AbstractIdeExecutor;
import dev.nokee.runtime.darwin.internal.locators.XcodebuildLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XcodebuildExecutor extends AbstractIdeExecutor<XcodebuildExecutor> {
	public static final class XcodebuildAction {
		private XcodebuildAction() {}
		public static final IdeAction BUILD = IdeAction.of("build");
		public static final IdeAction CLEAN = IdeAction.of("clean");
		public static final IdeAction TEST = IdeAction.of("test");
	}

	private final List<String> args = new ArrayList<String>();
	private final TestFile testDirectory;

	public XcodebuildExecutor(TestFile testDirectory) {
		this(testDirectory, testDirectory.file(".xcode-derived"));
	}

	private XcodebuildExecutor(TestFile testDirectory, File derivedData) {
		super(testDirectory, XcodebuildExecutor.class, getXcodebuildProvider());
		// TODO: Restore this feature
//		addArguments("-derivedDataPath", derivedData.getAbsolutePath());
		this.testDirectory = testDirectory;
	}

	public static CommandLineToolProvider getXcodebuildProvider() {
		return CommandLineToolProvider.from(() -> CommandLineTool.of(new XcodebuildLocator().findAll("xcodebuild").iterator().next().getPath()));
	}

	protected IdeAction getDefaultIdeAction() {
		return XcodebuildAction.BUILD;
	}

	public XcodebuildExecutor withProject(XcodeIdeProjectFixture xcodeProject) {
		return addArguments("-project", xcodeProject.getDir().getAbsolutePath());
	}

	public XcodebuildExecutor withWorkspace(XcodeIdeWorkspaceFixture xcodeWorkspace) {
		return addArguments("-workspace", xcodeWorkspace.getDir().getAbsolutePath());
	}

	public XcodebuildExecutor withScheme(String schemeName) {
		return addArguments("-scheme", schemeName);
	}

	public XcodebuildExecutor withConfiguration(String configurationName) {
		return addArguments("-configuration", configurationName);
	}
}
