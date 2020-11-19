package dev.nokee.platform.nativebase.internal.plugins.testers;

import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.writeStringToFile;

public abstract class WellBehavedPluginBasicHelpTasksTester {
	protected abstract String getQualifiedPluginIdUnderTest();

	@TempDir
	File workingDirectory;

	@BeforeEach
	void aBuildScriptApplyingPlugin() throws Exception {
		File buildFile = new File(workingDirectory, "build.gradle");
		StringWriter content = new StringWriter();
		PrintWriter out = new PrintWriter(content);
		out.println("plugins {");
		out.println("    id '" + getQualifiedPluginIdUnderTest() + "'");
		out.println("}");
		writeStringToFile(buildFile, content.toString(), StandardCharsets.UTF_8);
	}

	private void executeTask(String task) {
		GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(workingDirectory).withTasks(task);

		// TODO: Should this also be a feature of Runner Kit
//		Assume.assumeNotNull(this.getClass().getResource("plugin-under-test-metadata.properties"));

		// TODO: Should this be a feature of Runner Kit
		if (this.getClass().getResource("plugin-under-test-metadata.properties") == null) {
			runner = runner.withPluginClasspath(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new).collect(Collectors.toList()));
		} else {
			runner = runner.withPluginClasspath();
		}

		runner.build();
	}

	@Test
	void canExecuteHelpTask() {
		executeTask("help");
	}

	@Test
	void canExecuteTasksTask() {
		executeTask("tasks");
	}
}
