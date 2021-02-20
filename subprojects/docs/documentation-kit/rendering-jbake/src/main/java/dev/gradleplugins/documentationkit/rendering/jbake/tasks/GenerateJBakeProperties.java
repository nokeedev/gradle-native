package dev.gradleplugins.documentationkit.rendering.jbake.tasks;

import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class GenerateJBakeProperties extends DefaultTask {
	@Input
	public abstract MapProperty<String, Object> getConfigurations();

	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	@TaskAction
	private void doGenerate() throws IOException {
		val properties = new Properties();
		properties.putAll(getConfigurations().get());
		try (val outStream = new FileOutputStream(getOutputFile().get().getAsFile())) {
			properties.store(outStream, null);
		}
	}
}
