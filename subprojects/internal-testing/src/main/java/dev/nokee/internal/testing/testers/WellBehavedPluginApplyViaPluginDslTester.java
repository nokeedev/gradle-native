package dev.nokee.internal.testing.testers;

import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class WellBehavedPluginApplyViaPluginDslTester {
	protected abstract String getQualifiedPluginIdUnderTest();

	@Test
	void canApplyByIdViaPluginDsl(@TempDir File workingDirectory) throws Exception {
		File buildFile = new File(workingDirectory, "build.gradle");
		StringWriter content = new StringWriter();
		PrintWriter out = new PrintWriter(content);
		out.println("plugins {");
		out.println("    id '" + getQualifiedPluginIdUnderTest() + "'");
		out.println("}");
		FileUtils.writeStringToFile(buildFile, content.toString(), StandardCharsets.UTF_8);

		configureRunner(GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(workingDirectory)).build();
	}

	private GradleRunner configureRunner(GradleRunner runner) {
		// TODO: Should this be a feature of Runner Kit
		if (this.getClass().getResource("plugin-under-test-metadata.properties") == null) {
			return runner.withPluginClasspath(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new).collect(Collectors.toList()));
		}
		return runner.withPluginClasspath();
	}
}
