package dev.nokee.ide.visualstudio.fixtures;

import com.google.common.collect.Lists;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionResult;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.CommandLineToolProvider;
import dev.nokee.ide.fixtures.AbstractIdeExecutor;
import dev.nokee.runtime.nativebase.internal.locators.MSBuildLocator;
import dev.nokee.runtime.nativebase.internal.locators.VswhereLocator;
import org.apache.commons.io.FileUtils;
import org.gradle.internal.UncheckedException;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;

public class MSBuildExecutor extends AbstractIdeExecutor<MSBuildExecutor> {
	public static final class MSBuildAction {
		private MSBuildAction() {}
		public static final IdeAction BUILD = IdeAction.of("build");
		public static final IdeAction CLEAN = IdeAction.of("clean");
	}

	private String projectName;

	public MSBuildExecutor(TestFile workingDirectory) {
		super(workingDirectory, MSBuildExecutor.class, getMsbuildProvider());
	}

	public static CommandLineToolProvider getMsbuildProvider() {
		return CommandLineToolProvider.from(() -> CommandLineTool.of(new MSBuildLocator(() -> CommandLineTool.of(new VswhereLocator().findAll("vswhere").iterator().next().getPath())).findAll("msbuild").iterator().next().getPath()));
	}

	protected IdeAction getDefaultIdeAction() {
		return MSBuildAction.BUILD;
	}

	public MSBuildExecutor withSolution(VisualStudioIdeSolutionFixture visualStudioSolution) {
		addArguments(visualStudioSolution.getSolutionFile().getAbsolutePath());
		return this;
	}

	public MSBuildExecutor withConfiguration(String configurationName) {
		addArguments("/p:Configuration=" + configurationName);
		return this;
	}

	public MSBuildExecutor withProject(String projectName) {
		this.projectName = projectName;
		return this;
	}

	private File getOutputsDirectory() {
		return getWorkingDirectory().file("output");
	}

	private void cleanupOutputDirectory() {
		try {
			FileUtils.deleteDirectory(getOutputsDirectory());
			getOutputsDirectory().mkdir();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private List<ExecutionOutput> getOutputFiles() {
		List<ExecutionOutput> outputFiles = Lists.newArrayList();
		for (File executionDir : getOutputsDirectory().listFiles()) {
			if (executionDir.isDirectory()) {
				outputFiles.add(new ExecutionOutput(new File(executionDir, "output.txt"), new File(executionDir, "error.txt")));
			}
		}
		return outputFiles;
	}

	@Override
	protected CommandLineToolExecutionResult doExecute() {
		cleanupOutputDirectory();
		return super.doExecute();
	}

	@Override
	protected ExecutionResult asExecutionResult(CommandLineToolExecutionResult result) {
		if (getOutputFiles().isEmpty()) {
			return OutputScrapingExecutionResult.from(trimLines(result.getStandardOutput().getAsString()), trimLines(result.getErrorOutput().getAsString()));
		} else {
			// TODO: Support multiple Gradle execution result
			Assert.assertThat(getOutputFiles().size(), Matchers.equalTo(1));
			ExecutionOutput output = getOutputFiles().iterator().next();
			String gradleStdout = fileContents(output.stdout);
			String gradleStderr = fileContents(output.stderr);

			System.out.println(gradleStdout);
			System.out.println(gradleStderr);

			return OutputScrapingExecutionResult.from(trimLines(gradleStdout), trimLines(gradleStderr));
		}
	}

	@Override
	protected ExecutionFailure asExecutionFailure(CommandLineToolExecutionResult result) {
		if (getOutputFiles().isEmpty()) {
			return OutputScrapingExecutionFailure.from(trimLines(result.getStandardOutput().getAsString()), trimLines(result.getErrorOutput().getAsString()));
		} else {
			Assert.assertThat(getOutputFiles().size(), Matchers.equalTo(1));
			ExecutionOutput output = getOutputFiles().iterator().next();
			String gradleStdout = fileContents(output.stdout);
			String gradleStderr = fileContents(output.stderr);

			System.out.println(gradleStdout);
			System.out.println(gradleStderr);

			return OutputScrapingExecutionFailure.from(trimLines(gradleStdout), trimLines(gradleStderr));
		}
	}

	private static String fileContents(File file) {
		try {
			// TODO this should not be using the default charset because it's not an input and might introduce flakiness
			return FileUtils.readFileToString(file, Charset.defaultCharset());
		} catch (IOException e) {
			throw UncheckedException.throwAsUncheckedException(e);
		}
	}

	private String trimLines(String s) {
		return s.replaceAll("\r?\n\\s+", "\n");
	}

	@Override
	protected String asArgument(IdeAction action) {
		String result = "";
		if (projectName != null) {
			result = projectName;
		}
		if (!(projectName != null && action == MSBuildAction.BUILD)) {
			if (projectName != null) {
				result += ":";
			}
			result += action.toString();
		}
		return "/t:" + result;
	}

	private static class ExecutionOutput {
		private final File stdout;
		private final File stderr;

		public ExecutionOutput(File stdout, File stderr) {
			this.stdout = stdout;
			this.stderr = stderr;
		}
	}
}
