package dev.gradleplugins.documentationkit.tasks;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.documentationkit.RepositorySerializer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.net.URI;

public abstract class GenerateRepositoriesManifestTask extends DefaultTask {
	private final RepositorySerializer serializer = new RepositorySerializer();

	@Input
	public abstract SetProperty<URI> getRepositories();

	@OutputFile
	public abstract RegularFileProperty getManifestFile();

	@TaskAction
	private void doGenerate() throws Exception {
		serializer.serialize(ImmutableList.copyOf(getRepositories().get()), getManifestFile().get().getAsFile());
	}
}
