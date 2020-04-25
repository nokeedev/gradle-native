package dev.nokee.docs;

import dev.nokee.docs.tasks.ProcessAsciidoctor;
import dev.nokee.docs.types.Asciidoctor;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DocumentationExtension {
	private final NamedDomainObjectContainer<Sample> samples;
	private final SourceSet<Asciidoctor> contentSourceSet;

	@Inject
	public DocumentationExtension(ObjectFactory objects, TaskContainer tasks, SourceSetFactory sourceSetFactory, Provider<String> documentationVersion, Provider<String> projectVersion) {
		this.samples = objects.domainObjectContainer(Sample.class, name -> objects.newInstance(Sample.class, name, sourceSetFactory));

		TaskProvider<ProcessAsciidoctor> processDocsTask = tasks.register("processDocsAsciidoctors", ProcessAsciidoctor.class, task -> {
			task.getSource().setDir("src/docs").include("**/*.adoc").exclude("samples/*/*").include("samples/index.adoc");
			task.getRelativePath().set(documentationVersion.map(it -> "docs/" + it));
			task.getVersion().set(projectVersion);
			task.getMinimumGradleVersion().set("6.2.1");
		});
		contentSourceSet = sourceSetFactory.newSourceSet("docsContent", Asciidoctor.class, processDocsTask, processDocsTask.flatMap(it -> it.getOutputDirectory()), "**/*.adoc");
	}

	public NamedDomainObjectContainer<Sample> getSamples() {
		return samples;
	}

	public void samples(Action<? super NamedDomainObjectContainer<Sample>> action) {
		action.execute(samples);
	}

	public SourceSet<Asciidoctor> getContentSourceSet() {
		return contentSourceSet;
	}
}
