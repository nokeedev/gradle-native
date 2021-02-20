package dev.gradleplugins.documentationkit.internal;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.documentationkit.*;
import dev.gradleplugins.documentationkit.dsl.docbook.AssembleDslDocTask;
import dev.gradleplugins.documentationkit.dsl.source.ExtractDslMetaDataTask;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.ModelActions.mutate;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class DslReferenceDocumentationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodes.of(components).get(NodeRegistrationFactoryRegistry.class).registerFactory(of(DslReferenceDocumentation.class), name -> buildLanguageReference(name, project));
		val dslReference = components.register("dslReference", DslReferenceDocumentation.class, component -> {
			component.getPermalink().set("dsl");
			component.getClassDocbookDirectory().set(project.file("src/docs/dsl"));
		});

		val configurationRegistry = ProjectConfigurationRegistry.forProject(project);
		configurationRegistry.createIfAbsent("contentElements", asConsumable()
			.andThen(forUsage("jbake-content"))
			.andThen(attribute(DocsType.DOCS_TYPE_ATTRIBUTE, named("jbake-content"))))
			.getOutgoing().artifact(dslReference.flatMap(component -> component.getDslContent().getContentDirectory()));
	}

	public static NodeRegistration<DslReferenceDocumentation> buildLanguageReference(String name, Project project) {
		val configurationRegistry = ProjectConfigurationRegistry.forProject(project);
		val namingScheme = ConfigurationNamingScheme.forComponent(ComponentName.of(name));
		val descriptionScheme = ConfigurationDescriptionScheme.forComponent(ComponentName.of(name));
		val resolvableBucketRegistrationFactory = new ResolvableDependencyBucketRegistrationFactory(configurationRegistry, namingScheme, descriptionScheme);
		val consumableBucketRegistrationFactory = new ConsumableDependencyBucketRegistrationFactory(configurationRegistry, namingScheme, descriptionScheme);
		val taskRegistry = new TaskRegistry(project.getTasks(), TaskNamingScheme.forComponent(name));
		val pluginManager = project.getPluginManager();
		val configurations = project.getConfigurations();
		val projectLayout = project.getLayout();

		val factory = new DslReferenceDocumentationModelElementRegistrationFactory(new DslMetaDataModelElementRegistrationFactory(project.getObjects()), new DslContentModelElementRegistrationFactory(project.getObjects()), new DslReferenceDocumentationDependenciesModelElementRegistrationFactory(project.getObjects(), resolvableBucketRegistrationFactory, consumableBucketRegistrationFactory), project.getObjects());

		return factory.create(name)

			// Wiring
			.action(self(ModelNodes.discover()).apply(node -> {
				node.getDescendant("dslContent").applyTo(self(discover(context -> {
					val artifact = context.projectionOf(of(DslContent.class));
					val generateTask = taskRegistry.register("generate", AssembleDslDocTask.class, task -> {
						task.getClassMetaDataFiles().from(artifact.flatMap(elementsOf(DslContent::getClassMetaDataFiles)));
						task.getPluginsMetaDataFile().convention(projectLayout.getProjectDirectory().file("src/docs/dsl/plugins.xml"));
						task.getClassNames().convention(artifact.flatMap(DslContent::getClassNames));

						task.getClassDocbookDirectories().from(artifact.flatMap(elementsOf(DslContent::getClassDocbookDirectories)));
						task.getTemplateFile().convention(artifact.flatMap(DslContent::getTemplateFile));
						task.getDestinationDirectory().convention(projectLayout.getBuildDirectory().dir("tmp/" + task.getName()));
					});

					val stageTask = taskRegistry.register("stageDsl", Sync.class, task -> {
						task.from(generateTask.flatMap(AssembleDslDocTask::getDestinationDirectory), spec -> spec.into(artifact.flatMap(DslContent::getPermalink)));
						task.setDestinationDir(projectLayout.getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
					});
					artifact.configure(it -> it.getContentDirectory().fileProvider(stageTask.map(Sync::getDestinationDir)).disallowChanges());
				})));
				node.getDescendant("dslMetaData").applyTo(self(discover(context -> {
					val artifact = context.projectionOf(of(DslMetaData.class));
					val extractTask = taskRegistry.register("dslMetaData", ExtractDslMetaDataTask.class, task -> {
						task.source(artifact.flatMap(it -> it.getSources().getAsFileTree().getElements()));
						task.getDestinationFile().set(projectLayout.getBuildDirectory().file("tmp/" + task.getName() + "/dslMetaData.bin"));
					});

					val assembleTask = taskRegistry.register("stage", Sync.class, task -> {
						task.from(extractTask.flatMap(ExtractDslMetaDataTask::getDestinationFile));
						task.from(artifact.flatMap(elementsOf(DslMetaData::getClassDocbookFiles)), spec -> spec.into("dslMetaDataClasses"));
						task.setDestinationDir(projectLayout.getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
					});
					artifact.configure(it -> it.getExtractedMetaDataFile().fileProvider(assembleTask.map(Sync::getDestinationDir)).disallowChanges());
				})));
			}))
			.action(self(mutate(of(DslReferenceDocumentation.class), component -> {
				if (pluginManager.hasPlugin("java")) {
					component.getDependencies().getDslMetaData().getAsConfiguration().extendsFrom(configurations.getByName("implementation"));
					component.getDependencies().getDslMetaDataElements().getAsConfiguration().extendsFrom(configurations.getByName("implementation"));
				}

				component.getDslMetaData().getSources().from(mainJavaSourcesIfAvailable(project.getExtensions()));
				component.getDslMetaData().getClassDocbookFiles().from(component.getClassDocbookDirectory());

				component.getDependencies().getDslMetaDataElements().artifact(component.getDslMetaData().getExtractedMetaDataFile());

				component.getDslContent().getPermalink().set(component.getPermalink());
				component.getDslContent().getClassNames().set(component.getClassDocbookDirectory().map(directory -> {
					if (directory.getAsFile().exists()) {
						return Arrays.asList(directory.getAsFile().list()).stream().filter(it -> it.endsWith(".json")).map(FilenameUtils::removeExtension).collect(Collectors.toList());
					}
					return ImmutableList.of();
				}));
				component.getDslContent().getClassMetaDataFiles()
					.from(component.getDslMetaData().getExtractedMetaDataFile().file("dslMetaData.bin").map(it -> {
						if (it.getAsFile().exists()) {
							return it;
						}
						return ImmutableList.of();
					}))
					.from(component.getDependencies().getDslMetaData().getAsLenientFileCollection().getElements().map(transformEach(it -> new File(it.getAsFile(), "dslMetaData.bin"))));
				component.getDslContent().getClassDocbookDirectories().from(component.getClassDocbookDirectory()); // TODO: Maybe pick it up from dslMetaData artifact
				component.getDslContent().getClassDocbookDirectories().from(component.getDependencies().getDslMetaData().getAsLenientFileCollection().getElements().map(transformEach(it -> new File(it.getAsFile(), "dslMetaDataClasses"))));
			})));
	}

	private static Callable<Object> mainJavaSourcesIfAvailable(ExtensionContainer extensions) {
		return () -> {
			val sourceSets = extensions.findByType(SourceSetContainer.class);
			if (sourceSets != null) {
				val mainSourceSet = sourceSets.findByName("main");
				if (mainSourceSet != null) {
					return mainSourceSet.getJava().getAsFileTree();
				}
			}
			return ImmutableList.of();
		};
	}

	private static <IN> Transformer<Provider<Set<FileSystemLocation>>, IN> elementsOf(Function<IN, ? extends FileCollection> mapper) {
		return new Transformer<Provider<Set<FileSystemLocation>>, IN>() {
			@Override
			public Provider<Set<FileSystemLocation>> transform(IN in) {
				return mapper.apply(in).getElements();
			}
		};
	}
}
