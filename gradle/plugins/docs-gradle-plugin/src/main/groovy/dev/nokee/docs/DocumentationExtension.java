package dev.nokee.docs;

import dev.nokee.docs.tasks.ProcessAsciidoctor;
import dev.nokee.docs.types.Asciidoctor;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class DocumentationExtension {
	private final NamedDomainObjectContainer<Sample> samples;

	@Inject
	public DocumentationExtension(ObjectFactory objects, TaskContainer tasks, Provider<String> documentationVersion, Provider<String> projectVersion) {
		getMinimumGradleVersion().convention("6.2.1");
		this.samples = objects.domainObjectContainer(Sample.class, name -> objects.newInstance(Sample.class, name));

		TaskProvider<ProcessAsciidoctor> processDocsTask = tasks.register("processDocsAsciidoctors", ProcessAsciidoctor.class, task -> {
			task.getSource().setDir("src/docs").include("**/*.adoc").exclude("samples/*/*").include("samples/index.adoc");

			task.getAttributes().put("jbake-version", projectVersion);
			task.getAttributes().put("toc", "");
			task.getAttributes().put("toclevels", "1");
			task.getAttributes().put("toc-title", "Contents");
			task.getAttributes().put("icons", "font");
			task.getAttributes().put("idprefix", "");
			task.getAttributes().put("jbake-status", "published");
			task.getAttributes().put("encoding", "utf-8");
			task.getAttributes().put("lang", "en-US");
			task.getAttributes().put("sectanchors", "true");
			task.getAttributes().put("sectlinks", "true");
			task.getAttributes().put("linkattrs", "true");
			task.getAttributes().put("gradle-user-manual", getMinimumGradleVersion().map(version -> "https://docs.gradle.org/" + version + "/userguide"));
			task.getAttributes().put("gradle-language-reference", getMinimumGradleVersion().map(version -> "https://docs.gradle.org/" + version + "/dsl"));
			task.getAttributes().put("gradle-api-reference", getMinimumGradleVersion().map(version -> "https://docs.gradle.org/" + version + "/javadoc"));
			task.getAttributes().put("gradle-guides", "https://guides.gradle.org/");
			task.getAttributes().put("includedir", ".");
		});
		getContentSourceSet().from(processDocsTask.flatMap(ProcessAsciidoctor::getOutputDirectory));

		// TODO: Copy doc to relative path
	}

	public NamedDomainObjectContainer<Sample> getSamples() {
		return samples;
	}

	public void samples(Action<? super NamedDomainObjectContainer<Sample>> action) {
		action.execute(samples);
	}

	public abstract ConfigurableFileCollection getContentSourceSet();

	public abstract Property<String> getMinimumGradleVersion();
}
