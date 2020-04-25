package dev.nokee.docs;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.nokee.docs.tasks.GenerateSamplesContentTask;
import dev.nokee.docs.types.Asciidoctor;
import dev.nokee.docs.types.PublicData;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class Sample implements Named {
	private final String name;
	private final SourceSet<PublicData> templateSourceSet;
	private final SourceSet<PublicData> groovyDslSourceSet;
	private final SourceSet<PublicData> kotlinDslSourceSet;
	private final SourceSet<Asciidoctor> contentSourceSet;

	private static String getTemplateClassOrNull(Provider<SourceElement> template) {
		if (template.isPresent()) {
			return template.get().getClass().getCanonicalName();
		}
		return null;
	}

	@Inject
	public Sample(String name, ProjectLayout layout, TaskContainer tasks, SourceSetFactory sourceSetFactory) {
		this.name = name;

		String taskName = "generate" + getNameAsCamelCase() + "SampleContent";
		Provider<Directory> outputDirectory = layout.getBuildDirectory().dir("tmp/" + taskName);
		TaskProvider<Task> generateSampleCodeTask = tasks.register(taskName, task -> {
			task.getInputs().property("templateClass", getTemplateClassOrNull(getTemplate())).optional(true);
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.getOutputs().dir(outputDirectory);
			task.onlyIf(it -> getTemplate().isPresent());
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					getTemplate().get().writeToProject(TestFile.of(outputDirectory.get().getAsFile()));
				}
			});
		});
		templateSourceSet = sourceSetFactory.newSourceSet(GUtil.toLowerCamelCase(name) + "Template", PublicData.class, generateSampleCodeTask, outputDirectory, "**/*");
		groovyDslSourceSet = sourceSetFactory.newSourceSet(GUtil.toLowerCamelCase(name) + "GroovyDsl", PublicData.class);
		groovyDslSourceSet.getSource().setDir(layout.getProjectDirectory().dir("src/docs/samples/" + name + "/" + Dsl.GROOVY_DSL.getName()));
		kotlinDslSourceSet = sourceSetFactory.newSourceSet(GUtil.toLowerCamelCase(name) + "KotlinDsl", PublicData.class);
		kotlinDslSourceSet.getSource().setDir(layout.getProjectDirectory().dir("src/docs/samples/" + name + "/" + Dsl.KOTLIN_DSL.getName()));

		TaskProvider<GenerateSamplesContentTask> generateSamplesContentTask = tasks.register("process" + getNameAsCamelCase() + "SampleAsciidoctors", GenerateSamplesContentTask.class, task -> {
			task.getSourceDirectory().set(layout.getProjectDirectory().file("src/docs/samples/" + getName() + "/README.adoc"));
			task.getArchiveBaseName().set(GUtil.toCamelCase(getName()));
			task.getPermalink().set(getName());
		});
		contentSourceSet = sourceSetFactory.newSourceSet(GUtil.toLowerCamelCase(name) + "Content", Asciidoctor.class, generateSamplesContentTask, generateSamplesContentTask.flatMap(task -> task.getOutputDirectory()), "**/*.adoc");
	}

	@Override
	public String getName() {
		return name;
	}

	public String getNameAsCamelCase() {
		return GUtil.toCamelCase(name);
	}

	public String getNameAsLowerCamelCase() {
		return GUtil.toLowerCamelCase(name);
	}

	public abstract Property<SourceElement> getTemplate();

	public SourceSet<PublicData> getTemplateSourceSet() {
		return templateSourceSet;
	}

	public SourceSet<PublicData> getGroovyDslSourceSet() {
		return groovyDslSourceSet;
	}

	public SourceSet<PublicData> getKotlinDslSourceSet() {
		return kotlinDslSourceSet;
	}

	public SourceSet<Asciidoctor> getContentSourceSet() {
		return contentSourceSet;
	}
}
