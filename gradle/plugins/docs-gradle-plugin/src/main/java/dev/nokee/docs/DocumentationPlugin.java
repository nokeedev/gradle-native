package dev.nokee.docs;

import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import dev.gradleplugins.internal.plugins.SpockFrameworkTestSuiteBasePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public abstract class DocumentationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply("net.nokeedev.jbake-site");
		project.getPluginManager().apply("nokeedocs.samples");
		project.getPluginManager().apply("nokeedocs.user-manual");
		project.getPluginManager().apply("nokeedocs.javadoc");
		project.getPluginManager().apply(SpockFrameworkTestSuiteBasePlugin.class);

		project.getComponents().add(project.getObjects().newInstance(GroovySpockFrameworkTestSuite.class, "docsTest", project.getExtensions().getByType(SourceSetContainer.class).create("docsTest")));

		val documentation = project.getExtensions().create("documentation", DocumentationExtension.class);
		documentation.getExtensions().add("samples", project.getExtensions().getByName("samples"));

//		tasks.named("bakePreview", JBakeServeTask.class, task -> {
//			task.setInput(getLayout().getBuildDirectory().dir("generated/baked").get().getAsFile());
//		});

		samples(project, samples -> {
			samples.configureEach(new IncludeDslContent(project));
			samples.configureEach(new IncludeSourceTemplate(project));
		});
	}

	private static void samples(Project project, Action<? super NamedDomainObjectContainer<dev.gradleplugins.dockit.samples.Sample>> action) {
		action.execute((NamedDomainObjectContainer<dev.gradleplugins.dockit.samples.Sample>) project.getExtensions().getByName("samples"));
	}
}
