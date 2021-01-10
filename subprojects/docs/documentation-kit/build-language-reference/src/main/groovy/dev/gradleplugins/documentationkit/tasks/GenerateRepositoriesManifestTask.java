package dev.gradleplugins.documentationkit.tasks;

import com.google.gson.GsonBuilder;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public abstract class GenerateRepositoriesManifestTask extends DefaultTask {
	@Input
	public abstract SetProperty<URI> getRepositories();

	@OutputFile
	public abstract RegularFileProperty getManifestFile();

	@TaskAction
	private void doGenerate() throws IOException {
		val gson = new GsonBuilder().create();
		FileUtils.write(getManifestFile().get().getAsFile(), gson.toJson(getRepositories().get()), StandardCharsets.UTF_8);
	}
}
