package dev.gradleplugins.documentationkit.internal;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.documentationkit.*;
import dev.gradleplugins.documentationkit.tasks.GenerateDependenciesManifestTask;
import dev.gradleplugins.documentationkit.tasks.GenerateRepositoriesManifestTask;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryRegistry;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.repositories.UrlArtifactRepository;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;

// expose/consume sources, dependencies and repositories
public class ApiReferenceDocumentationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodes.of(components).get(NodeRegistrationFactoryRegistry.class).registerFactory(of(ApiReferenceDocumentation.class), name -> apiReference(name, project));
		components.register("apiReference", ApiReferenceDocumentation.class);
	}

	private static NodeRegistration<ApiReferenceDocumentation> apiReference(String name, Project project) {
		val configurationRegistry = ProjectConfigurationRegistry.forProject(project);
		val namingScheme = ConfigurationNamingScheme.forComponent(ComponentName.of(name));
		val descriptionScheme = ConfigurationDescriptionScheme.forComponent(ComponentName.of(name));
		val resolvableBucketRegistrationFactory = new ResolvableDependencyBucketRegistrationFactory(configurationRegistry, namingScheme, descriptionScheme);
		val consumableBucketRegistrationFactory = new ConsumableDependencyBucketRegistrationFactory(configurationRegistry, namingScheme, descriptionScheme);
		val declarableBucketRegistrationFactory = new DeclarableDependencyBucketRegistrationFactory(configurationRegistry, namingScheme, descriptionScheme);
		val taskRegistry = new TaskRegistry(project.getTasks(), TaskNamingScheme.forComponent(name));
		val projectLayout = project.getLayout();
		val configurations = project.getConfigurations();
		val pluginManager = project.getPluginManager();
		val repositories = project.getRepositories();
		val extensions = project.getExtensions();
		val objects = project.getObjects();

		val factory = new ApiReferenceDocumentationModelElementRegistrationFactory(new ApiReferenceManifestModelElementRegistrationFactory(project.getObjects()), new ApiReferenceDocumentationDependenciesModelElementRegistrationFactory(consumableBucketRegistrationFactory, resolvableBucketRegistrationFactory, declarableBucketRegistrationFactory));

		return factory.create(name)
			.action(self(ModelNodes.discover()).apply(node -> {
				node.getDescendant("manifest").applyTo(self(discover(context -> {
					val artifact = context.projectionOf(of(ApiReferenceManifest.class));
					val dependenciesManifestTask = taskRegistry.register(TaskName.of("generate", "dependenciesManifest"), GenerateDependenciesManifestTask.class, task -> {
						task.getDependencies().set(artifact.flatMap(ApiReferenceManifest::getDependencies));
						task.getManifestFile().set(projectLayout.getBuildDirectory().file("tmp/" + task.getName() + "/dependencies.manifest"));
					});

					val repositoriesManifestTask = taskRegistry.register(TaskName.of("generate", "repositoriesManifest"), GenerateRepositoriesManifestTask.class, task -> {
						task.getRepositories().set(artifact.flatMap(ApiReferenceManifest::getRepositories));
						task.getManifestFile().set(projectLayout.getBuildDirectory().file("tmp/" + task.getName() + "/repositories.manifest"));
					});

					val syncTask = taskRegistry.register(TaskName.of("assemble", "manifest"), Sync.class, task -> {
						task.from(artifact.map(ApiReferenceManifest::getSources), spec -> spec.into("sources"));
						task.from(dependenciesManifestTask.flatMap(GenerateDependenciesManifestTask::getManifestFile));
						task.from(repositoriesManifestTask.flatMap(GenerateRepositoriesManifestTask::getManifestFile));
						task.setDestinationDir(projectLayout.getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
					});
					artifact.configure(it -> it.getDestinationLocation().fileProvider(syncTask.map(Sync::getDestinationDir)).disallowChanges());
				})));
				node.getDescendant("manifest").applyTo(self(mutate(of(ApiReferenceManifest.class), new Consumer<ApiReferenceManifest>() {
						@Override
						public void accept(ApiReferenceManifest artifact) {
							artifact.getRepositories().addAll(forCallable(this::repositoryUrls));
							artifact.getSources().from(asCallable(this::mainJavaSourcesIfAvailable));
							artifact.getDependencies().addAll(forCallable(this::externalCompileClasspathDependenciesIfAvailable));
						}

						private Iterable<URI> repositoryUrls() {
							return repositories.stream()
								.filter(UrlArtifactRepository.class::isInstance)
								.map(UrlArtifactRepository.class::cast)
								.map(UrlArtifactRepository::getUrl)
								.collect(Collectors.toList());
						}

						private List<FileTree> mainJavaSourcesIfAvailable() {
							if (pluginManager.hasPlugin("java")) {
								return ImmutableList.of(extensions
									.getByType(SourceSetContainer.class)
									.getByName("main")
									.getJava()
									.getAsFileTree());
							}
							return ImmutableList.of();
						}

						private Iterable<Dependency> externalCompileClasspathDependenciesIfAvailable() {
							if (pluginManager.hasPlugin("java")) {
								return configurations
									.getByName("compileClasspath")
									.getAllDependencies()
									.stream()
									.filter(ExternalDependency.class::isInstance)
									.collect(Collectors.toList());
							}
							return ImmutableList.of();
						}
					})));
				val apiNode = node.getDescendant("dependencies").getDescendant("api");
				node.getDescendant("dependencies").getDescendant("manifestElements").applyTo(self(ModelNodes.discover()).apply(executeUsingProjection(of(Configuration.class), c -> {
							c.getAttributes().attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, "api-reference-manifest"));
							c.extendsFrom(apiNode.get(Configuration.class));
						})));
				node.getDescendant("dependencies").getDescendant("manifest").applyTo(self(ModelNodes.discover()).apply(executeUsingProjection(of(Configuration.class), c -> {
							c.getAttributes().attribute(USAGE_ATTRIBUTE, objects.named(Usage.class, "api-reference-manifest"));
							c.extendsFrom(apiNode.get(Configuration.class));
						})));
			}))

			// Wire
			.action(self(mutate(of(ApiReferenceDocumentation.class), component -> {
				component.getDependencies().getManifestElements().artifact(component.getManifest().getDestinationLocation());
			})));
	}

	private static <T> Callable<T> asCallable(Callable<T> callable) {
		return callable;
	}

	private static <T> Provider<T> forCallable(Callable<T> callable) {
		return ProviderUtils.supplied(callable);
	}
}
