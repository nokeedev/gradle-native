package dev.nokee.docs;

import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import dev.gradleplugins.internal.plugins.SpockFrameworkTestSuiteBasePlugin;
import dev.nokee.docs.tasks.DotCompile;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;
import org.jbake.gradle.JBakeExtension;
import org.jbake.gradle.JBakeServeTask;

import javax.inject.Inject;

public abstract class DocumentationPlugin implements Plugin<Project> {
	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract DependencyHandler getDependencyHandler();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract ObjectFactory getObjects();

	private Provider<Directory> compileDotToPng() {
		TaskProvider<DotCompile> compileDotTask = getTasks().register("compileDocsDot", DotCompile.class, task -> {
			task.getSource().setDir("src/docs").include("**/*.dot").exclude("**/samples/**");
		});
		return compileDotTask.flatMap(DotCompile::getOutputDirectory);
	}

	@Override
	public void apply(Project project) {
		ObjectFactory objects = project.getObjects();
		TaskContainer tasks = project.getTasks();
		SoftwareComponentContainer components = project.getComponents();
		ProjectLayout layout = project.getLayout();
		ConfigurationContainer configurations = project.getConfigurations();
		ProviderFactory providers = project.getProviders();
		DependencyHandler dependencies = project.getDependencies();
		Provider<String> projectVersion = providers.provider(() -> project.getVersion().toString());
		Provider<String> documentationVersion = toVersion(project);

		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply("org.jbake.site");
		project.getPluginManager().apply(SpockFrameworkTestSuiteBasePlugin.class);

		components.add(objects.newInstance(GroovySpockFrameworkTestSuite.class, "docsTest", project.getExtensions().getByType(SourceSetContainer.class).create("docsTest")));

		LegacyDocumentationExtension extension = project.getExtensions().create("documentation", LegacyDocumentationExtension.class, documentationVersion, projectVersion);

		// Staging for all documentation
		TaskProvider<Sync> stageDocumentationTask = tasks.register("stageDocumentation", Sync.class, task -> {
			task.dependsOn("bake");
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(getLayout().getBuildDirectory().dir("generated/baked").get().getAsFile());
			task.setIncludeEmptyDirs(false);

			// Copy images from samples (TODO: it should be done somewhere else)
			task.from("src/docs/samples", spec -> {
				spec.include("**/*.gif");
				spec.into("docs/" + documentationVersion.get() + "/samples");
			});

			// Copy images from manual (TODO: it should be done somewhere else)
			task.from("src/docs/manual", spec -> {
				spec.include("**/*.png");
				spec.into("docs/" + documentationVersion.get() + "/manual");
			});
		});
		tasks.named("bakePreview", JBakeServeTask.class, task -> {
			task.setInput(getLayout().getBuildDirectory().dir("generated/baked").get().getAsFile());
		});

		TaskProvider<Task> assembleDocumentationTask = tasks.register("assembleDocumentation", task -> task.dependsOn(stageDocumentationTask));

		// Staging for JBake
		Provider<Directory> stageBakeDirectory = getLayout().getBuildDirectory().dir("staging");
		TaskProvider<Sync> stageBakeTask = tasks.register("stageBake", Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(stageBakeDirectory.get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});
		JBakeExtension jBakeExtension = project.getExtensions().getByType(JBakeExtension.class);
		jBakeExtension.setSrcDirName("build/staging");
		tasks.named("bake", task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.getInputs().files(stageBakeDirectory);
			task.dependsOn(stageBakeTask);
		});
		stageDocumentationTask.configure(task -> {
			Provider<Directory> bakedFiles = getLayout().getBuildDirectory().dir(jBakeExtension.getDestDirName());
			task.getInputs().files(bakedFiles);
			task.from(bakedFiles);
		});

		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		// SAMPLE
		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		TaskProvider<Sync> assembleSampleZipsTask = tasks.register("assembleSampleZips", Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(getLayout().getBuildDirectory().dir("generated/zips").get().getAsFile());
		});

		TaskProvider<Sync> stageSamplesTask = tasks.register("stageSamples", Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(getLayout().getBuildDirectory().dir("generated/samples").get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});
		stageBakeTask.configure(task -> {
			task.from(stageSamplesTask.map(Sync::getDestinationDir), spec -> spec.into("content/docs/" + documentationVersion.get() + "/samples"));
		});

		TaskProvider<Task> assembleSamplesTask = tasks.register("assembleSamples", task -> {
			task.dependsOn(stageSamplesTask);
		});

		extension.getSamples().configureEach(sample -> {
			sample.getProductVersion().set(projectVersion);
			sample.getMinimumGradleVersion().set("6.2.1");
			sample.getPluginManagementBlock().convention(PluginManagementBlock.nokee(projectVersion.get()));
			stageSamplesTask.configure(task -> {
				task.from(sample.getStageSourceSet(), spec -> spec.into(sample.getName()));
			});

			stageDocumentationTask.configure(task -> {
				task.from(sample.getAsciinemaSourceSet(), spec -> spec.into("docs/" + documentationVersion.get() + "/samples/" + sample.getName()));
			});

			assembleSampleZipsTask.configure(task -> {
				task.from(sample.getArchiveSourceSet());
			});
		});

		String jbakeTemplatesDirectory = "src/jbake/templates";
		stageBakeTask.configure(task -> {
			task.from(extension.getContentSourceSet(), spec -> spec.into(documentationVersion.map(it -> "content/docs/" + it)));
			task.from("src/jbake/content", spec -> spec.into("content"));
			task.from("src/jbake/assets", spec -> spec.into("assets"));
			task.from(jbakeTemplatesDirectory, spec -> spec.into("templates"));
			task.from("src/jbake/jbake.properties");
		});

		// *.dot -> *.png
		Provider<Directory> pngSourceSet = compileDotToPng();
		stageDocumentationTask.configure(task -> {
			task.from(pngSourceSet, spec -> spec.into(documentationVersion.map(it -> "docs/" + it)));
		});

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
		stageBakeTask.configure(task -> {
			task.getInputs().files(content);
			task.getInputs().files(assets);
			task.getInputs().files(templates);

			task.from(content, spec -> spec.into(documentationVersion.map(it -> "content/docs/" + it)));
			task.from(assets, spec -> spec.into("assets"));
			task.from(templates, spec -> spec.into("templates"));
		});

		// Configurations (outgoing)
		// contentElements is handled by documentation-kit
		Configuration assetsElements = configurations.create("assetsElements", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-assets"));
			});
//			//Disabling assets export because it doesn't make any sense at the moment.
////			project.afterEvaluate(proj -> {
////				components.withType(JBakeAssetSourceSet.class).stream().map(LanguageSourceSet::getSource).forEach(source -> {
////					source.getFiles().forEach(file -> {
////						configuration.getOutgoing().getVariants().maybeCreate("directory").artifact(file, it -> {
////							it.setType(ArtifactTypeDefinition.DIRECTORY_TYPE);
////							it.builtBy(source);
////						});
////					});
////				});
////			});
		});
////		TaskProvider<Zip> zipJbakeAssetsTask = tasks.register("zipJbakeAssets", Zip.class, task ->{
////			task.from(components.withType(JBakeAssetSourceSet.class).stream().map(LanguageSourceSet::getSource).collect(Collectors.toList()));
////			task.getArchiveClassifier().set("assets");
////		});
////		assetsElements.getOutgoing().artifact(zipJbakeAssetsTask);
//
		Configuration templatesElements = configurations.create("templatesElements", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-templates"));
			});
			configuration.getOutgoing().getVariants().maybeCreate("directory").artifact(project.file(jbakeTemplatesDirectory), it -> {
				it.setType(ArtifactTypeDefinition.DIRECTORY_TYPE);
			});
		});
		TaskProvider<Zip> zipJbakeTemplatesTask = tasks.register("zipJbakeTemplates", Zip.class, task -> {
			task.from(jbakeTemplatesDirectory);
			task.getArchiveClassifier().set("templates");
		});
		templatesElements.getOutgoing().artifact(zipJbakeTemplatesTask);

		Configuration propertiesElements = configurations.create("propertiesElements", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-properties"));
			});
			configuration.getOutgoing().artifact(project.file("src/jbake/jbake.properties"));
		});

		AdhocComponentWithVariants jbake = getSoftwareComponentFactory().adhoc("jbake");
		jbake.addVariantsFromConfiguration(assetsElements, this::configureVariant);
		jbake.addVariantsFromConfiguration(templatesElements, this::configureVariant);
		jbake.addVariantsFromConfiguration(propertiesElements, this::configureVariant);
		project.getComponents().add(jbake);


		Configuration bakedElements = configurations.create("bakedElements", configuration -> {
			configuration.setCanBeResolved(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-baked"));
			});
			project.afterEvaluate(proj -> {
				configuration.getOutgoing().getVariants().create("directory").artifact(stageDocumentationTask.map(Sync::getDestinationDir), it -> {
					it.setType(ArtifactTypeDefinition.DIRECTORY_TYPE);
					it.builtBy(stageDocumentationTask);
				});
			});
		});
		TaskProvider<Zip> zipJbakeBakedTask = tasks.register("zipJbakeBaked", Zip.class, task -> {
			task.from(stageDocumentationTask.map(it -> it.getDestinationDir()));
			task.getArchiveClassifier().set("baked");
		});
		bakedElements.getOutgoing().artifact(zipJbakeBakedTask);
		AdhocComponentWithVariants baked = getSoftwareComponentFactory().adhoc("baked");
		baked.addVariantsFromConfiguration(bakedElements, this::configureVariant);
		project.getComponents().add(baked);
	}

	@Inject
	protected abstract SoftwareComponentFactory getSoftwareComponentFactory();

	private Provider<String> toVersion(Project project) {
		if (project.getVersion().toString().contains("-")) {
			return getProviders().provider(() -> "nightly");
		}
		return getProviders().provider(() -> project.getVersion().toString());
	}

	private void configureVariant(ConfigurationVariantDetails variantDetails) {
		if (hasUnpublishableArtifactType(variantDetails.getConfigurationVariant())) {
			variantDetails.skip();
		}
	}

	public static boolean hasUnpublishableArtifactType(ConfigurationVariant element) {
		for (PublishArtifact artifact : element.getArtifacts()) {
			if (ArtifactTypeDefinition.DIRECTORY_TYPE.equals(artifact.getType())) {
				return true;
			}
		}
		return false;
	}
}
