package dev.gradleplugins.documentationkit.tasks;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.documentationkit.DependencySerializer;
import dev.nokee.utils.SpecUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public abstract class GenerateDependenciesManifestTask extends DefaultTask {
	private final DependencySerializer serializer;

	@Internal
	public abstract SetProperty<Dependency> getDependencies();

	@OutputFile
	public abstract RegularFileProperty getManifestFile();

	@Inject
	public GenerateDependenciesManifestTask(DependencyHandler dependencyFactory) {
		this.serializer = new DependencySerializer(dependencyFactory);
		getOutputs().upToDateWhen(SpecUtils.satisfyNone());
	}

	@TaskAction
	private void doGenerate() throws Exception {
		serializer.serialize(ImmutableList.copyOf(getDependencies().get()), getManifestFile().get().getAsFile());
	}
}
