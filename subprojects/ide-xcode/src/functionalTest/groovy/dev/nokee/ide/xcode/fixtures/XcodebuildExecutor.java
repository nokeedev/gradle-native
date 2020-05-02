package dev.nokee.ide.xcode.fixtures;

import dev.gradleplugins.test.fixtures.file.ExecOutput;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.nokee.ide.xcode.fixtures.IdeCommandLineUtil.buildEnvironment;
import static org.junit.Assert.assertTrue;

public class XcodebuildExecutor {
	public enum XcodeAction {
		BUILD,
		CLEAN,
		TEST;

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	private final List<String> args = new ArrayList<String>();
	private final TestFile testDirectory;

	public XcodebuildExecutor(TestFile testDirectory) {
		this(testDirectory, testDirectory.file(".xcode-derived"));
	}

	private XcodebuildExecutor(TestFile testDirectory, File derivedData) {
		// TODO: Restore this feature
//		addArguments("-derivedDataPath", derivedData.getAbsolutePath());
		this.testDirectory = testDirectory;
	}

	public XcodebuildExecutor withProject(XcodeIdeProjectFixture xcodeProject) {
		TestFile projectDir = xcodeProject.getDir();
		projectDir.assertIsDirectory();
		return addArguments("-project", projectDir.getAbsolutePath());
	}

	public XcodebuildExecutor withWorkspace(XcodeIdeWorkspaceFixture xcodeWorkspace) {
		TestFile workspaceDir = xcodeWorkspace.getDir();
		workspaceDir.assertIsDirectory();
		return addArguments("-workspace", workspaceDir.getAbsolutePath());
	}

	public XcodebuildExecutor withScheme(String schemeName) {
		return addArguments("-scheme", schemeName);
	}

	public XcodebuildExecutor withConfiguration(String configurationName) {
		return addArguments("-configuration", configurationName);
	}

	public XcodebuildExecutor withArgument(String arg) {
		this.args.add(arg);
		return this;
	}

	private XcodebuildExecutor addArguments(String... args) {
		this.args.addAll(Arrays.asList(args));
		return this;
	}

	public ExecOutput execute() {
		ExecOutput result = findXcodeBuild().execute(args, buildEnvironment(testDirectory));
		System.out.println(result.getOut());
		return result;
	}

	public ExecOutput executeAndExpectFailure() {
		System.out.println(args);
		ExecOutput result = findXcodeBuild().execWithFailure(args, buildEnvironment(testDirectory));
		System.out.println(result.getOut());
		System.out.println(result.getError());
		return result;
	}

	public ExecutionResult succeeds() {
		return succeeds(XcodeAction.BUILD);
	}

	public ExecutionResult succeeds(XcodeAction action) {
		withArgument(action.toString());
		ExecOutput result = findXcodeBuild().execute(args, buildEnvironment(testDirectory));
		System.out.println(result.getOut());
		return OutputScrapingExecutionResult.from(result.getOut(), result.getError());
	}

	public ExecutionFailure fails() {
		return fails(XcodeAction.BUILD);
	}

	public ExecutionFailure fails(XcodeAction action) {
		withArgument(action.toString());
		ExecOutput result = findXcodeBuild().execWithFailure(args, buildEnvironment(testDirectory));
		// stderr of Gradle is redirected to stdout of xcodebuild tool. To work around, we consider xcodebuild stdout and stderr as
		// the error output only if xcodebuild failed most likely due to Gradle.
		System.out.println(result.getOut());
		System.out.println(result.getError());
		return OutputScrapingExecutionFailure.from(result.getOut(), result.getError());
	}

	private TestFile findXcodeBuild() {
		TestFile xcodebuild = new TestFile("/usr/bin/xcodebuild");
		assertTrue("This test requires xcode to be installed in " + xcodebuild.getAbsolutePath(), xcodebuild.exists());
		return xcodebuild;
	}
}
