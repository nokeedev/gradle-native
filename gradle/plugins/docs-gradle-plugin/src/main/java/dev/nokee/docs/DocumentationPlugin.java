package dev.nokee.docs;

import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import dev.gradleplugins.internal.plugins.SpockFrameworkTestSuiteBasePlugin;
import dev.nokee.docs.tasks.DotCompile;
import dev.nokee.docs.tasks.GenerateSamplesContentTask;
import dev.nokee.docs.tasks.ProcessAsciidoctor;
import dev.nokee.docs.tasks.ProcessorTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jbake.gradle.JBakeExtension;

import java.util.stream.Collectors;

public class DocumentationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		ObjectFactory objects = project.getObjects();
		TaskContainer tasks = project.getTasks();
		SoftwareComponentContainer components = project.getComponents();
		ProjectLayout layout = project.getLayout();
		ConfigurationContainer configurations = project.getConfigurations();
		ProviderFactory providers = project.getProviders();

		project.getPluginManager().apply(SpockFrameworkTestSuiteBasePlugin.class);

		components.add(objects.newInstance(GroovySpockFrameworkTestSuite.class, "docsTest"));

		// Source sets
		TaskProvider<GenerateSamplesContentTask> generateSamplesContentTask = tasks.register("generateSamplesAsciidoctors", GenerateSamplesContentTask.class);
		TaskProvider<ProcessAsciidoctor> processSamplesTask = tasks.register("processSamplesAsciidoctors", ProcessAsciidoctor.class, task -> {
			task.dependsOn(generateSamplesContentTask);
			task.getSource().setDir(generateSamplesContentTask.flatMap(it -> it.getOutputDirectory())).include("**/*");
			task.getRelativePath().set("docs/nightly/samples");
			task.getVersion().set(providers.provider(() -> project.getVersion().toString()));
			task.getMinimumGradleVersion().set("6.2.1");
		});

		TaskProvider<ProcessAsciidoctor> processDocsTask = tasks.register("processDocsAsciidoctors", ProcessAsciidoctor.class, task -> {
			task.getSource().setDir("src/docs").include("**/*.adoc").exclude("samples/*/*").include("samples/index.adoc");
			task.getRelativePath().set("docs/nightly");
			task.getVersion().set(providers.provider(() -> project.getVersion().toString()));
			task.getMinimumGradleVersion().set("6.2.1");
		});
		JBakeContentSourceSet contentSourceSet = objects.newInstance(JBakeContentSourceSet.class, "jbakeContent");
		contentSourceSet.getSource().from(processDocsTask.flatMap(ProcessorTask::getOutputDirectory));
		contentSourceSet.getSource().from(processSamplesTask.flatMap(ProcessorTask::getOutputDirectory));
		components.add(contentSourceSet);

		TaskProvider<DotCompile> compileDotTask = tasks.register("compileDocsDot", DotCompile.class, task -> {
			task.getSource().setDir("src/docs").include("**/*.dot");
			task.getRelativePath().set("docs/nightly");
		});
		JBakeAssetSourceSet assetSourceSet = objects.newInstance(JBakeAssetSourceSet.class, "jbakeAssets");
		assetSourceSet.getSource().from(compileDotTask.flatMap(ProcessorTask::getOutputDirectory));
		assetSourceSet.getSource().from("src/jbake/assets");
		components.add(assetSourceSet);

		JBakeTemplateSourceSet templateSourceSet = objects.newInstance(JBakeTemplateSourceSet.class, "jbakeTemplate");
		templateSourceSet.getSource().from("src/jbake/templates");
		components.add(templateSourceSet);

		// Configurations (incoming)
		Configuration content = configurations.create("content", configuration -> {
			configuration.setCanBeResolved(true);
			configuration.setCanBeConsumed(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-content"));
			});
		});
		Configuration assets = configurations.create("assets", configuration -> {
			configuration.setCanBeConsumed(true);
			configuration.setCanBeConsumed(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-assets"));
			});
		});
		Configuration templates = configurations.create("templates", configuration -> {
			configuration.setCanBeConsumed(true);
			configuration.setCanBeConsumed(false);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-templates"));
			});
		});

		// Stage task
		Provider<Directory> stagingDirectory = layout.getBuildDirectory().dir("staging");
		TaskProvider<Sync> stageTask = tasks.register("stage", Sync.class, task -> {
			task.into(stagingDirectory);
			task.from(content, spec -> spec.into("content"));
			task.from(assets, spec -> spec.into("assets"));
			task.from(templates, spec -> spec.into("templates"));
			task.from(components.withType(JBakeContentSourceSet.class).stream().map(LanguageSourceSet::getSource).collect(Collectors.toList()), spec -> spec.into("content"));
			task.from(components.withType(JBakeAssetSourceSet.class).stream().map(LanguageSourceSet::getSource).collect(Collectors.toList()), spec -> spec.into("assets"));
			task.from(components.withType(JBakeTemplateSourceSet.class).stream().map(LanguageSourceSet::getSource).collect(Collectors.toList()), spec -> spec.into("templates"));
			task.from("src/jbake/jbake.properties");
		});
		project.getExtensions().configure(JBakeExtension.class, extension -> {
			extension.setSrcDirName("build/staging");
		});
		tasks.named("bake", it -> it.dependsOn(stageTask));

		// Configurations (outgoing)
		configurations.create("contentElements", configuration -> {
			configuration.setCanBeResolved(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-content"));
			});
			project.afterEvaluate(proj -> {
				components.withType(JBakeContentSourceSet.class).stream().map(LanguageSourceSet::getSource).forEach(source -> {
					source.getFiles().forEach(file -> {
						configuration.getOutgoing().artifact(file, it -> it.builtBy(source.getBuiltBy()));
					});
				});
			});
		});
		configurations.create("assetsElements", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-assets"));
			});
			project.afterEvaluate(proj -> {
				components.withType(JBakeAssetSourceSet.class).stream().map(LanguageSourceSet::getSource).forEach(source -> {
					source.getFiles().forEach(file -> {
						configuration.getOutgoing().artifact(file, it -> it.builtBy(source.getBuiltBy()));
					});
				});
			});
		});
		configurations.create("templatesElements", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-templates"));
			});
			project.afterEvaluate(proj -> {
				components.withType(JBakeTemplateSourceSet.class).stream().map(LanguageSourceSet::getSource).forEach(source -> {
					source.getFiles().forEach(file -> {
						configuration.getOutgoing().artifact(file, it -> it.builtBy(source.getBuiltBy()));
					});
				});
			});
		});
		configurations.create("propertiesElements", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-properties"));
			});
			configuration.getOutgoing().artifact(project.file("src/jbake/jbake.properties"));
		});
	}
}
