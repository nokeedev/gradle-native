package dev.nokee.internal.testing.testers;

import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.join;

public abstract class WellBehavedPluginTaskConfigurationAvoidanceTester {
	protected abstract String getQualifiedPluginIdUnderTest();

	protected List<String> getRealizedTaskPaths() {
		return Collections.singletonList(":help");
	}

	@Test
	void doesNotRealizeTask(@TempDir File testDirectory) throws IOException {
		StringWriter content = new StringWriter();
		PrintWriter out = new PrintWriter(content);
		out.println("plugins {");
		out.println("    id '" + getQualifiedPluginIdUnderTest() + "'");
		out.println("}");
		out.println();
		out.println("def configuredTasks = []");
		out.println("tasks.configureEach {");
		out.println("    configuredTasks << it");
		out.println("}");
		out.println();
		out.println("gradle.buildFinished {");
		out.println("    def configuredTaskPaths = configuredTasks*.path");
		out.println();
		out.println("    // TODO: Log warning if getRealizedTaskPaths() is different than ':help'");
		out.println("    configuredTaskPaths.removeAll([" + join(", ", getRealizedQuotedTaskPaths()) + "])");
		out.println("    assert configuredTaskPaths == []");
		out.println("}");

		File buildFile = new File(testDirectory, "build.gradle");
		FileUtils.writeStringToFile(buildFile, content.toString(), StandardCharsets.UTF_8);

		GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory).configureUsing(this::configureRunner).withTasks("help").build();
	}

	private GradleRunner configureRunner(GradleRunner runner) {
		// TODO: Should this be a feature of Runner Kit
		if (this.getClass().getResource("plugin-under-test-metadata.properties") == null) {
			return runner.withPluginClasspath(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new).collect(Collectors.toList()));
		}
		return runner.withPluginClasspath();
	}

	private List<String> getRealizedQuotedTaskPaths() {
		return getRealizedTaskPaths().stream().map(this::quoted).collect(Collectors.toList());
	}

	private String quoted(String s) {
		return "'" + s + "'";
	}
}
