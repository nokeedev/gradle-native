/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nokeebuild;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.JavaVersion;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.HasConfigurableAttributes;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.credentials.AwsCredentials;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.provider.PropertyInternal;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaCompiler;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension.from;
import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static java.util.Optional.ofNullable;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.JVM_CLASS_DIRECTORY;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY;
import static org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE;
import static org.gradle.api.attributes.Bundling.EXTERNAL;
import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE;
import static org.gradle.api.attributes.Category.DOCUMENTATION;
import static org.gradle.api.attributes.Category.LIBRARY;
import static org.gradle.api.attributes.DocsType.DOCS_TYPE_ATTRIBUTE;
import static org.gradle.api.attributes.DocsType.SOURCES;
import static org.gradle.api.attributes.LibraryElements.CLASSES;
import static org.gradle.api.attributes.LibraryElements.JAR;
import static org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE;
import static org.gradle.api.attributes.LibraryElements.RESOURCES;
import static org.gradle.api.attributes.Usage.JAVA_RUNTIME;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;
import static org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE;
import static org.gradle.api.plugins.BasePlugin.BUILD_GROUP;
import static org.gradle.api.plugins.JavaBasePlugin.DOCUMENTATION_GROUP;

@SuppressWarnings("UnstableApiUsage")
abstract /*final*/ class UniversalModuleDevelopmentPlugin implements Plugin<Project> {
	@Inject
	public UniversalModuleDevelopmentPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("java-base");
		project.getPluginManager().apply("nokeebuild.continuous-integration");

		repositories(project, mavenCentral());
		repositories(project, gradlePluginDevelopment());
		repositories(project, mavenLocal());

		final NamedDomainObjectProvider<SourceSet> main = sourceSets(project, register("main"));
		main.configure(usesLowestJvmCompatibilitySupportedByGradle(project, "6.2.1"));
		main.configure(withApiDependencies(project));
		main.configure(withRuntimeDependencies(project));
		main.configure(withClassesJar(project));
		main.configure(withJavadocJar(project));
		main.configure(withSourcesJar(project));
		main.configure(asJavaSoftwareComponent(project));
		main.configure(sourceSetTasks(project, javadoc(new UseUtf8Encoding())));
		main.configure(sourceSetDependencies(project, useLombok(lombokVersion(project))));

		final NamedDomainObjectProvider<SourceSet> test = sourceSets(project, register("test"));
		test.configure(usesLatestJvmCompatibility(project));
		test.configure(testedComponent(project, main));
		test.configure(registerDefaultTestSuite(project));
		test.configure(withJUnitJupiter(project, junitVersion(project)));

//		project.getPluginManager().withPlugin("maven-publish", appliedPlugin -> {
//			final MavenArtifactRepository nokeeRelease = publishing(project, repositories(nokeeRelease(project)));
//			final MavenArtifactRepository nokeeSnapshot = publishing(project, repositories(nokeeSnapshot(project)));
//
//			final NamedDomainObjectProvider<MavenPublication> snapshot = publishing(project, publications(register("snapshot", MavenPublication.class)));
//			snapshot.configure(version(set("version-snapshot")));
//			snapshot.configure(groupId(set(fromProjectGroupId(project)))); // TODO: Maybe afterEvaluate
//			snapshot.configure(publishJavaSoftwareComponent(project));
//			snapshot.configure(publicationTasks(project, doesNotPublishTo(nokeeRelease)));
//
//
//			final NamedDomainObjectProvider<MavenPublication> release = publishing(project, publications(register("release", MavenPublication.class)));
//			release.configure(version(set("0.5.0")));
//			release.configure(groupId(set(fromProjectGroupId(project)))); // TODO: Maybe afterEvaluate
//			release.configure(publishJavaSoftwareComponent(project));
//			release.configure(publicationTasks(project, doesNotPublishTo(nokeeSnapshot)));
//		});

		tasks(project, named("quickTest")).configure(dependsOn(test.flatMap(sourceSetTasks(project, defaultTestTask()))));

		// continuous-integration (quick/full/sanity check)
		// quality -> spotless
		// licensing -> ide & spotless
	}

	private static <SELF extends Task> Action<SELF> dependsOn(Object path, Object... paths) {
		return self -> {
			self.dependsOn(path);
			self.dependsOn(paths);
		};
	}

	private static Provider<String> fromProjectGroupId(Project project) {
		return project.provider(project.getGroup()::toString);
	}

	private static Action<SourceSet> registerDefaultTestSuite(Project project) {
		return sourceSet -> {
			final NamedDomainObjectProvider<Test> testTask = project.getTasks().register(sourceSet.getTaskName(null, null), Test.class);
			testTask.configure(task -> {
				task.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
				task.setClasspath(sourceSet.getRuntimeClasspath());
				task.getJavaLauncher().set(targetCompatibility(project, sourceSet).flatMap(toJavaLauncher(project)));
				// TODO: configure module path
			});
			testTask.configure(belongsTo(sourceSet));
		};
	}

	private static Provider<JavaLanguageVersion> targetCompatibility(Project project, SourceSet sourceSet) {
		return project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class).flatMap(it -> project.provider(() -> JavaLanguageVersion.of(JavaVersion.toVersion(it.getTargetCompatibility()).getMajorVersion())));
	}

	private static Transformer<Provider<JavaLauncher>, JavaLanguageVersion> toJavaLauncher(Project project) {
		return languageVersion -> project.getExtensions().getByType(JavaToolchainService.class).launcherFor(spec -> spec.getLanguageVersion().set(languageVersion));
	}

	private static Transformer<Provider<JavaCompiler>, JavaLanguageVersion> toJavaCompiler(Project project) {
		return languageVersion -> project.getExtensions().getByType(JavaToolchainService.class).compilerFor(spec -> spec.getLanguageVersion().set(languageVersion));
	}

	private static <SELF extends ExtensionAware> Action<SELF> belongsTo(Object owner) {
		return self -> self.getExtensions().add("belongTo", owner);
	}

	private static <SELF extends Publication> Action<SELF> publishJavaSoftwareComponent(Project project) {
		return self -> {
			if (self instanceof MavenPublication) {
				((MavenPublication) self).from(project.getComponents().getByName("java"));
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF extends PublicationTasks> Action<SELF> doesNotPublishTo(MavenArtifactRepository repository) {
		return self -> {
			self.publishTo(repository).configure(task -> task.setEnabled(false));
		};
	}

	public static Action<Publication> publicationTasks(Project project, Action<? super PublicationTasks> action) {
		return publication -> {
			action.execute(new PublicationTasks() {
				@Override
				public TaskProvider<PublishToMavenRepository> publishTo(MavenArtifactRepository repository) {
					return project.getTasks().named("publish" + StringUtils.capitalize(publication.getName()) + "PublicationTo" + StringUtils.capitalize(repository.getName()) + "Repository", PublishToMavenRepository.class);
				}
			});
		};
	}

	public interface PublicationTasks {
		TaskProvider<PublishToMavenRepository> publishTo(MavenArtifactRepository repository);
//		<S extends Task> void configureEach(Class<S> type, Action<? super S> action);
	}

	private static <SELF> BiFunction<SELF, RepositoryHandler, MavenArtifactRepository> nokeeSnapshot(Project project) {
		return (self, repositories) -> {
			return repositories.maven(repository -> {
				repository.setName("nokeeSnapshot");
				repository.setUrl(project.getProviders().gradleProperty(repository.getName() + "Url").orElse(""));
				repository.credentials(AwsCredentials.class);
			});
		};
	}

	private static <SELF> BiFunction<SELF, RepositoryHandler, MavenArtifactRepository> nokeeRelease(Project project) {
		return (self, repositories) -> {
			return repositories.maven(repository -> {
				repository.setName("nokeeRelease");
				repository.setUrl(project.getProviders().gradleProperty(repository.getName() + "Url").orElse(""));
				repository.credentials(AwsCredentials.class);
			});
		};
	}

	private static <SELF, OUT> Function<SELF, OUT> repositories(BiFunction<? super SELF, ? super RepositoryHandler, OUT> action) {
		return self -> {
			if (self instanceof PublishingExtension) {
				return action.apply(self, ((PublishingExtension) self).getRepositories());
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF> Action<SELF> version(BiConsumer<? super SELF, ? super PropertyAdapter<String>> action) {
		return self -> {
			if (self instanceof MavenPublication) {
				action.accept(self, new PlainPropertyAdapter<>(((MavenPublication) self)::getVersion, ((MavenPublication) self)::setVersion));
			} else if (self instanceof Project) {
				action.accept(self, new PlainPropertyAdapter<>(() -> ((Project) self).getVersion().toString(), value -> ((Project) self).setVersion(value)));
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF> Action<SELF> groupId(BiConsumer<? super SELF, ? super PropertyAdapter<String>> action) {
		return self -> {
			if (self instanceof MavenPublication) {
				action.accept(self, new PlainPropertyAdapter<>(((MavenPublication) self)::getGroupId, ((MavenPublication) self)::setGroupId));
			} else if (self instanceof Project) {
				action.accept(self, new PlainPropertyAdapter<>(() -> ((Project) self).getGroup().toString(), value -> ((Project) self).setGroup(value)));
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static Action<PublishingExtension> publications(BiConsumer<? super PublishingExtension, ? super PublicationContainer> action) {
		return self -> action.accept(self, self.getPublications());
	}

	private static <OUT> Function<PublishingExtension, OUT> publications(Function<? super PublicationContainer, OUT> mapper) {
		return self -> mapper.apply(self.getPublications());
	}

	private static void publishing(Project project, Action<? super PublishingExtension> action) {
		action.execute((PublishingExtension) project.getExtensions().getByName("publishing"));
	}

	private static <OUT> OUT publishing(Project project, Function<? super PublishingExtension, OUT> mapper) {
		return mapper.apply((PublishingExtension) project.getExtensions().getByName("publishing"));
	}

	public static String lombokVersion(Project project) {
		return Objects.toString(project.property("lombokVersion"), null);
	}

	private static <SELF extends HasCompileOnlyDependencyBucket & HasAnnotationProcessorDependencyBucket> Action<SELF> useLombok(String version) {
		return self -> {
			self.getAnnotationProcessor().addDependency("org.projectlombok:lombok:" + version);
			self.getCompileOnly().addDependency("org.projectlombok:lombok:" + version);
		};
	}

	public interface HasImplementationDependencyBucket {
		DependencyBucket getImplementation();
	}

	public interface HasAnnotationProcessorDependencyBucket {
		DependencyBucket getAnnotationProcessor();
	}

	public interface HasCompileOnlyDependencyBucket {
		DependencyBucket getCompileOnly();
	}

	public interface HasJavadocTask {
		TaskProvider<Javadoc> getJavadoc();
	}

	public interface HasTestTasks {
		TaskProvider<Test> getTestTask();
	}

	public interface View<T> {
		<S extends T> void configureEach(Class<S> type, Action<? super S> action);
	}

	public interface JvmComponentTasks extends HasJavadocTask, HasTestTasks, View<Task> {

	}

	public static Action<SourceSet> sourceSetTasks(Project project, Action<? super JvmComponentTasks> action) {
		return sourceSet -> {
			action.execute(new JvmComponentTasks() {
				@Override
				public TaskProvider<Test> getTestTask() {
					return project.getTasks().named("test", Test.class);
				}

				@Override
				public <S extends Task> void configureEach(Class<S> type, Action<? super S> action) {
					project.getTasks().withType(type).configureEach(belongingTo(sourceSet, action));
				}

				@Override
				public TaskProvider<Javadoc> getJavadoc() {
					return project.getTasks().named(sourceSet.getJavadocTaskName(), Javadoc.class);
				}
			});
		};
	}

	public static <OUT> Transformer<OUT, SourceSet> sourceSetTasks(Project project, Function<? super JvmComponentTasks, OUT> action) {
		return sourceSet -> {
			return action.apply(new JvmComponentTasks() {
				@Override
				public TaskProvider<Test> getTestTask() {
					return project.getTasks().named("test", Test.class);
				}

				@Override
				public <S extends Task> void configureEach(Class<S> type, Action<? super S> action) {
					project.getTasks().withType(type).configureEach(belongingTo(sourceSet, action));
				}

				@Override
				public TaskProvider<Javadoc> getJavadoc() {
					return project.getTasks().named(sourceSet.getJavadocTaskName(), Javadoc.class);
				}
			});
		};
	}

	private static <SELF extends HasTestTasks> Function<SELF, NamedDomainObjectProvider<Test>> defaultTestTask() {
		return HasTestTasks::getTestTask;
	}

	private static <T extends ExtensionAware> Action<T> belongingTo(Object target, Action<? super T> action) {
		return self -> {
			if (self.getExtensions().findByName("belongTo") == target) {
				action.execute(self);
			}
		};
	}

	public static <SELF extends HasJavadocTask> Action<SELF> javadoc(Action<? super Javadoc> action) {
		return self -> self.getJavadoc().configure(action);
	}

	public static Action<SourceSet> sourceSetDependencies(Project project, Action<? super JvmComponentDependencies> action) {
		return sourceSet -> {
			action.execute(new JvmComponentDependencies() {
				@Override
				public ExternalModuleDependency testFixtures(CharSequence dependencyNotation) {
					return (ExternalModuleDependency) project.getDependencies().testFixtures(dependencyNotation);
				}

				@Override
				@SuppressWarnings("unchecked")
				public <D extends ModuleDependency> D testFixtures(D dependency) {
					return (D) project.getDependencies().testFixtures(dependency);
				}

				@Override
				public DependencyBucket getCompileOnly() {
					return new DependencyBucket() {
						@Override
						public void addDependency(CharSequence dependencyNotation) {
							project.getDependencies().add(sourceSet.getCompileOnlyConfigurationName(), dependencyNotation);
						}

						@Override
						public void addDependency(Dependency dependency) {
							project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).getDependencies().add(dependency);
						}
					};
				}

				@Override
				public DependencyBucket getAnnotationProcessor() {
					return new DependencyBucket() {
						@Override
						public void addDependency(CharSequence dependencyNotation) {
							project.getDependencies().add(sourceSet.getAnnotationProcessorConfigurationName(), dependencyNotation);
						}

						@Override
						public void addDependency(Dependency dependency) {
							project.getConfigurations().getByName(sourceSet.getAnnotationProcessorConfigurationName()).getDependencies().add(dependency);
						}
					};
				}

				@Override
				public DependencyBucket getImplementation() {
					return new DependencyBucket() {
						@Override
						public void addDependency(CharSequence dependencyNotation) {
							project.getDependencies().add(sourceSet.getImplementationConfigurationName(), dependencyNotation);
						}

						@Override
						public void addDependency(Dependency dependency) {
							project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).getDependencies().add(dependency);
						}
					};
				}

				@Override
				@SuppressWarnings("unchecked")
				public <D extends ModuleDependency> D platform(D dependency) {
					return (D) project.getDependencies().platform(dependency);
				}

				@Override
				public ExternalModuleDependency platform(CharSequence dependencyNotation) {
					return platform((ExternalModuleDependency) project.getDependencies().create(dependencyNotation));
				}
			});
		};
	}

	public interface PlatformDependencyModifiers {
		<D extends ModuleDependency> D platform(D dependency);
		ExternalModuleDependency platform(CharSequence dependencyNotation);
	}

	public interface TestFixturesDependencyModifiers {
		ExternalModuleDependency testFixtures(CharSequence dependencyNotation);
		<D extends ModuleDependency> D testFixtures(D dependency);
	}

	public interface JvmComponentDependencies extends PlatformDependencyModifiers, TestFixturesDependencyModifiers, HasAnnotationProcessorDependencyBucket, HasImplementationDependencyBucket, HasCompileOnlyDependencyBucket {}

	public interface DependencyBucket {
		void addDependency(CharSequence dependencyNotation);
		void addDependency(Dependency dependency);
	}

	public static String junitVersion(Project project) {
		return Objects.toString(project.property("junitVersion"), null);
	}

	private static Action<SourceSet> testedComponent(Project project, Provider<SourceSet> component) {
		return sourceSet -> {
			// In theory, we should be able to write: project.files((Callable<Object>) component.map(SourceSet::getOutput)::get);
			//   but that doesn't work with IntelliJ and potentially Gradle in general.
			final FileCollection mainSourceSetOutput = component.map(SourceSet::getOutput).get();
			final FileCollection testSourceSetOutput = sourceSet.getOutput();

			sourceSet.setCompileClasspath(project.getObjects().fileCollection().from(mainSourceSetOutput, project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName())));
			sourceSet.setRuntimeClasspath(project.getObjects().fileCollection().from(testSourceSetOutput, mainSourceSetOutput, project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName())));

			project.getConfigurations().named(sourceSet.getImplementationConfigurationName()).configure(extendsFrom(component.map(it -> project.getConfigurations().getByName(it.getImplementationConfigurationName()))));
			project.getConfigurations().named(sourceSet.getRuntimeOnlyConfigurationName()).configure(extendsFrom(component.map(it -> project.getConfigurations().getByName(it.getRuntimeOnlyConfigurationName()))));
		};
	}

	private static Action<SourceSet> withJUnitJupiter(Project project, String version) {
		return sourceSet -> {
			sourceSetDependencies(project, useJUnitJupiter(version)).execute(sourceSet);
			sourceSetTasks(project, configureEach(Test.class, Test::useJUnitPlatform)).execute(sourceSet);
		};
	}

	private static <SELF extends HasImplementationDependencyBucket & PlatformDependencyModifiers> Action<SELF> useJUnitJupiter(String version) {
		return self -> {
			self.getImplementation().addDependency(self.platform("org.junit:junit-bom:" + version));
			self.getImplementation().addDependency("org.junit.jupiter:junit-jupiter");
		};
	}

	private static <SELF extends HasImplementationDependencyBucket> Action<SELF> useMockito(String version) {
		return self -> {
			self.getImplementation().addDependency("org.mockito:mockito-core:" + version);
		};
	}

	private static Action<SourceSet> javadoc(Project project, Action<? super Javadoc> action) {
		return sourceSet -> {
			project.getTasks().named(sourceSet.getJavadocTaskName(), Javadoc.class).configure(action);
		};
	}

	private static Action<SourceSet> asJavaSoftwareComponent(Project project) {
		return sourceSet -> {
			final AdhocComponentWithVariants component = ((ProjectInternal) project).getServices().get(SoftwareComponentFactory.class).adhoc("java");
			project.getComponents().add(component);
			ofNullable(project.getConfigurations().findByName(sourceSet.getApiElementsConfigurationName())).ifPresent(it -> {
				component.addVariantsFromConfiguration(it, t -> {
					if (t.getConfigurationVariant().getArtifacts().stream().anyMatch(a -> Arrays.asList(ArtifactTypeDefinition.DIRECTORY_TYPE, JVM_RESOURCES_DIRECTORY, JVM_CLASS_DIRECTORY).contains(a.getType()))) {
						t.skip();
					}
				});
			});
			ofNullable(project.getConfigurations().findByName(sourceSet.getRuntimeElementsConfigurationName())).ifPresent(it -> {
				component.addVariantsFromConfiguration(it, t -> {
					if (t.getConfigurationVariant().getArtifacts().stream().anyMatch(a -> Arrays.asList(ArtifactTypeDefinition.DIRECTORY_TYPE, JVM_RESOURCES_DIRECTORY, JVM_CLASS_DIRECTORY).contains(a.getType()))) {
						t.skip();
					}
				});
			});
			ofNullable(project.getConfigurations().findByName(sourceSet.getSourcesElementsConfigurationName())).ifPresent(it -> {
				component.addVariantsFromConfiguration(it, t -> {
					if (t.getConfigurationVariant().getArtifacts().stream().anyMatch(a -> Arrays.asList(ArtifactTypeDefinition.DIRECTORY_TYPE, JVM_RESOURCES_DIRECTORY, JVM_CLASS_DIRECTORY).contains(a.getType()))) {
						t.skip();
					}
				});
			});
			ofNullable(project.getConfigurations().findByName(sourceSet.getJavadocElementsConfigurationName())).ifPresent(it -> {
				component.addVariantsFromConfiguration(it, t -> {
					if (t.getConfigurationVariant().getArtifacts().stream().anyMatch(a -> Arrays.asList(ArtifactTypeDefinition.DIRECTORY_TYPE, JVM_RESOURCES_DIRECTORY, JVM_CLASS_DIRECTORY).contains(a.getType()))) {
						t.skip();
					}
				});
			});
		};
	}

	private static <OUT> OUT components(Project project, Function<? super SoftwareComponentContainer, OUT> mapper) {
		return mapper.apply(project.getComponents());
	}

	private static Function<SoftwareComponentContainer, NamedDomainObjectProvider<AdhocComponentWithVariants>> newAdhocComponent(Project project, String name) {
		return self -> {
			final AdhocComponentWithVariants result = ((ProjectInternal) project).getServices().get(SoftwareComponentFactory.class).adhoc(name);
			self.add(result);
			return self.named(result.getName(), AdhocComponentWithVariants.class);
		};
	}

	private static Action<SourceSet> withClassesJar(Project project) {
		return sourceSet -> {
			final NamedDomainObjectProvider<Jar> jarTask = tasks(project, register(sourceSet.getJarTaskName(), Jar.class));
			// or "the javadoc of the '<featureName>' feature"
			jarTask.configure(description(set("Assembles a jar archive containing the main classes.")));
			jarTask.configure(it -> it.from(sourceSet.getOutput()));
			jarTask.configure(destinationDirectory(set(project.getLayout().getBuildDirectory().dir("libs"))));
			jarTask.configure(group(set(BUILD_GROUP)));
			jarTask.configure(belongsTo(sourceSet));

			final NamedDomainObjectProvider<Configuration> apiElements = configurations(project, named(sourceSet.getApiElementsConfigurationName()));
			apiElements.configure(outgoing(artifact(jarTask)));

			final NamedDomainObjectProvider<Configuration> runtimeElements = configurations(project, named(sourceSet.getRuntimeElementsConfigurationName()));
			runtimeElements.configure(outgoing(artifact(jarTask)));
		};
	}

	private static Action<SourceSet> withJavadocJar(Project project) {
		return sourceSet -> {
			TaskProvider<Javadoc> javadocTask = project.getTasks().register(sourceSet.getJavadocTaskName(), Javadoc.class);
			// (featureName == null "main source code." : "'" + featureName + "' feature.
			javadocTask.configure(description(set("Generates Javadoc API documentation for " + sourceSet + ".")));
			javadocTask.configure(group(set(DOCUMENTATION_GROUP)));
			javadocTask.configure(it -> it.setClasspath(sourceSet.getOutput().plus(sourceSet.getCompileClasspath())));
			javadocTask.configure(it -> it.setSource(sourceSet.getAllJava()));
			javadocTask.configure(it -> it.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir("docs/" + it.getName()))));
			javadocTask.configure(belongsTo(sourceSet));

			final NamedDomainObjectProvider<Jar> javadocJarTask = tasks(project, register(sourceSet.getJavadocJarTaskName(), Jar.class));
			// or "the javadoc of the '<featureName>' feature"
			javadocJarTask.configure(description(set("Assembles a jar archive containing the main " + DocsType.JAVADOC + ".")));
			javadocJarTask.configure(it -> it.from(javadocTask.map(Javadoc::getDestinationDir)));
			javadocJarTask.configure(destinationDirectory(set(project.getLayout().getBuildDirectory().dir("libs"))));
			javadocJarTask.configure(classifier(set(DocsType.JAVADOC))); // TextUtil.camelToKebabCase
			javadocJarTask.configure(group(set(BUILD_GROUP)));
			javadocJarTask.configure(belongsTo(sourceSet));

			final NamedDomainObjectProvider<Configuration> javadocElements = configurations(project, register(sourceSet.getJavadocElementsConfigurationName()));
			javadocElements.configure(description(set(StringUtils.capitalize(DocsType.JAVADOC) + " elements for " + sourceSet + ".")));
			javadocElements.configure(it -> it.setVisible(false));
			javadocElements.configure(asConsumableBucket());
			javadocElements.configure(attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JAVA_RUNTIME)));
			javadocElements.configure(attribute(CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, DOCUMENTATION)));
			javadocElements.configure(attribute(BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, EXTERNAL)));
			javadocElements.configure(attribute(DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, DocsType.JAVADOC)));
			javadocElements.configure(outgoing(artifact(javadocJarTask)));
		};
	}

	private static Action<SourceSet> withSourcesJar(Project project) {
		return sourceSet -> {
			final NamedDomainObjectProvider<Jar> sourcesJarTask = tasks(project, register(sourceSet.getSourcesJarTaskName(), Jar.class));
			// or "the javadoc of the '<featureName>' feature"
			sourcesJarTask.configure(description(set("Assembles a jar archive containing the main " + SOURCES + ".")));
			sourcesJarTask.configure(it -> it.from(sourceSet.getAllSource()));
			sourcesJarTask.configure(destinationDirectory(set(project.getLayout().getBuildDirectory().dir("libs"))));
			sourcesJarTask.configure(classifier(set(SOURCES))); // TextUtil.camelToKebabCase
			sourcesJarTask.configure(group(set(BUILD_GROUP)));
			sourcesJarTask.configure(belongsTo(sourceSet));

			final NamedDomainObjectProvider<Configuration> sourcesElements = configurations(project, register(sourceSet.getSourcesElementsConfigurationName()));
			sourcesElements.configure(description(set(StringUtils.capitalize(SOURCES) + " elements for " + sourceSet + ".")));
			sourcesElements.configure(it -> it.setVisible(false));
			sourcesElements.configure(asConsumableBucket());
			sourcesElements.configure(attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JAVA_RUNTIME)));
			sourcesElements.configure(attribute(CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, DOCUMENTATION)));
			sourcesElements.configure(attribute(BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, EXTERNAL)));
			sourcesElements.configure(attribute(DOCS_TYPE_ATTRIBUTE, project.getObjects().named(DocsType.class, SOURCES)));
			sourcesElements.configure(outgoing(artifact(sourcesJarTask)));
		};
	}

	private static <SELF extends AbstractArchiveTask> Action<SELF> classifier(BiConsumer<? super SELF, ? super PropertyAdapter<String>> action) {
		return self -> action.accept(self, new GradlePropertyAdapter<>(self.getArchiveClassifier()));
	}

	private static <SELF extends Task> Action<SELF> group(BiConsumer<? super SELF, ? super PropertyAdapter<String>> action) {
		return self -> action.accept(self, new PlainPropertyAdapter<>(self::getGroup, self::setGroup));
	}

	private static <SELF/* extends AbstractArchiveTask*/> Action<SELF> destinationDirectory(BiConsumer<? super SELF, ? super PropertyAdapter<Directory>> action) {
		return self -> {
			if (self instanceof AbstractArchiveTask) {
				action.accept(self, new GradlePropertyAdapter<>(((AbstractArchiveTask) self).getDestinationDirectory()));
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static Action<SourceSet> withApiDependencies(Project project) {
		return sourceSet -> {
			final NamedDomainObjectProvider<Configuration> api = configurations(project, register(sourceSet.getApiConfigurationName()));
			api.configure(description(set("API dependencies for " + sourceSet + ".")));
			api.configure(asDeclarableBucket());
			api.configure(it -> it.setVisible(false));

			final NamedDomainObjectProvider<Configuration> compileOnlyApi = configurations(project, register(sourceSet.getCompileOnlyApiConfigurationName()));
			compileOnlyApi.configure(description(set("Compile-only API dependencies for " + sourceSet + ".")));
			compileOnlyApi.configure(asDeclarableBucket());
			compileOnlyApi.configure(it -> it.setVisible(false));

			configurations(project, named(sourceSet.getImplementationConfigurationName())).configure(extendsFrom(api));
			configurations(project, named(sourceSet.getCompileOnlyConfigurationName())).configure(extendsFrom(compileOnlyApi));

			final NamedDomainObjectProvider<Configuration> apiElements = configurations(project, register(sourceSet.getApiElementsConfigurationName()));
			apiElements.configure(description(set("API elements for " + sourceSet + ".")));
			apiElements.configure(asConsumableBucket());
			apiElements.configure(extendsFrom(api, compileOnlyApi));
			apiElements.configure(attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API)));
			apiElements.configure(attribute(CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, LIBRARY)));
			apiElements.configure(attribute(TARGET_JVM_VERSION_ATTRIBUTE, project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class).flatMap(toMajorVersion(project))));
			apiElements.configure(attribute(BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, EXTERNAL)));
			apiElements.configure(attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, JAR)));
//			apiElements.configure(outgoing(artifact(project.getTasks().named(sourceSet.getJarTaskName()))));
			apiElements.configure(outgoing(withClassDirectoryVariant(project, sourceSet)));
		};
	}

	private static Transformer<Provider<Integer>, JavaCompile> toMajorVersion(Project project) {
		return task -> {
			return task.getOptions().getRelease().orElse(project.provider(() -> {
				int releaseFlag = getReleaseOption(task.getOptions().getCompilerArgs());
				if (releaseFlag != 0) {
					return releaseFlag;
				} else {
					return Integer.parseInt(JavaVersion.toVersion(task.getTargetCompatibility()).getMajorVersion());
				}
			}));
		};
	}

	private static int getReleaseOption(List<String> compilerArgs) {
		int flagIndex = compilerArgs.indexOf("--release");
		if (flagIndex != -1 && flagIndex + 1 < compilerArgs.size()) {
			return Integer.parseInt(String.valueOf(compilerArgs.get(flagIndex + 1)));
		}
		return 0;
	}

	private static Action<SourceSet> withRuntimeDependencies(Project project) {
		return sourceSet -> {
			final NamedDomainObjectProvider<Configuration> runtimeElements = configurations(project, register(sourceSet.getRuntimeElementsConfigurationName()));
			runtimeElements.configure(description(set("Runtime elements for " + sourceSet + ".")));
			runtimeElements.configure(asConsumableBucket());
			runtimeElements.configure(extendsFrom(configurations(project, named(sourceSet.getImplementationConfigurationName())), configurations(project, named(sourceSet.getRuntimeOnlyConfigurationName()))));
			runtimeElements.configure(attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JAVA_RUNTIME)));
			runtimeElements.configure(attribute(CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, LIBRARY)));
			runtimeElements.configure(attribute(TARGET_JVM_VERSION_ATTRIBUTE, project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class).flatMap(toMajorVersion(project))));
			runtimeElements.configure(attribute(BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, EXTERNAL)));
			runtimeElements.configure(attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, JAR)));
			runtimeElements.configure(outgoing(withClassDirectoryVariant(project, sourceSet)));
			runtimeElements.configure(outgoing(withResourceDirectoryVariant(project, sourceSet)));
		};
	}

	private static <SELF> Action<SELF/* extends Task | Configuration*/> description(BiConsumer<? super SELF, PropertyAdapter<String>> action) {
		return self -> {
			if (self instanceof Task) {
				action.accept(self, new PlainPropertyAdapter<>(((Task) self)::getDescription, ((Task) self)::setDescription));
			} else if (self instanceof Configuration) {
				action.accept(self, new PlainPropertyAdapter<>(((Configuration) self)::getDescription, ((Configuration) self)::setDescription));
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF, T> BiConsumer<SELF, PropertyAdapter<T>> set(Object value) { // TODO: Figure out if it should only be Object or T
		return (self, property) -> property.setFromAnyObject(value);
	}

	private static Action<ConfigurationPublications> withClassDirectoryVariant(Project project, SourceSet sourceSet) {
		return outgoing -> {
			final NamedDomainObjectProvider<ConfigurationVariant> variant = outgoing.getVariants().register("classes");

			// We have to realize the ConfigurationVariant here because Gradle is missing some screws:
			variant.get();

			variant.configure(attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, CLASSES)));
			variant.configure(artifact(directory(project, classesDirectories(sourceSet), a -> a.setType(JVM_CLASS_DIRECTORY))));
		};
	}

	private static Action<ConfigurationPublications> withResourceDirectoryVariant(Project project, SourceSet sourceSet) {
		return outgoing -> {
			final NamedDomainObjectProvider<ConfigurationVariant> variant = outgoing.getVariants().register("resources");

			// We have to realize the ConfigurationVariant here because Gradle is missing some screws:
			variant.get();

			variant.configure(attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, RESOURCES)));
			variant.configure(artifact(sourceSet.getOutput().getResourcesDir(), a -> a.setType(JVM_RESOURCES_DIRECTORY)));
		};
	}

	private interface PropertyAdapter<T> {
		void setFromAnyObject(Object value);
		void conventionFromAnyObject(Object value);
	}

	@SuppressWarnings("unchecked")
	private static final class PlainPropertyAdapter<T> implements PropertyAdapter<T> {
		private final Supplier<T> getter;
		private final Consumer<T> setter;

		public PlainPropertyAdapter(Supplier<T> getter, Consumer<T> setter) {
			this.getter = getter;
			this.setter = setter;
		}

		public void setFromAnyObject(Object value) {
			if (value instanceof Provider) {
				setter.accept(((Provider<T>) value).get());
			} else {
				setter.accept((T) value);
			}
		}

		public void conventionFromAnyObject(Object value) {
			if (getter.get() == null) {
				setFromAnyObject(value);
			}
		}
	}

	private static Action<Configuration> outgoing(Action<? super ConfigurationPublications> action) {
		return self -> self.outgoing(action);
	}

	private static Callable<Object> classesDirectories(SourceSet sourceSet) {
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return sourceSet.getOutput().getClassesDirs();
			}

			@Override
			public String toString() {
				return sourceSet.getName() + "Classes";
			}
		};
	}

	private static Callable<Object> resourcesDirectories(SourceSet sourceSet) {
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return sourceSet.getOutput().getResourcesDir();
			}

			@Override
			public String toString() {
				return sourceSet.getName() + "Resources";
			}
		};
	}

	private static String temporaryDir(Task task) {
		return "tmp/" + task.getName();
	}

	private static void tasks(Project project, Action<? super TaskContainer> action) {
		action.execute(project.getTasks());
	}

	private static <OUT> OUT tasks(Project project, Function<? super TaskContainer, ? extends OUT> mapper) {
		return mapper.apply(project.getTasks());
	}

	private static <SELF/* extends ConfigurationVariant | ConfigurationPublications*/> Action<SELF> artifact(Function<? super SELF, ? extends PublishArtifact> mapper) {
		return self -> {
			if (self instanceof ConfigurationVariant) {
				((ConfigurationVariant) self).getArtifacts().add(mapper.apply(self));
			} else if (self instanceof ConfigurationPublications) {
				((ConfigurationPublications) self).getArtifacts().add(mapper.apply(self));
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF/* extends ConfigurationVariant | ConfigurationPublications*/> Action<SELF> artifact(Object notation) {
		return self -> {
			if (self instanceof ConfigurationVariant) {
				((ConfigurationVariant) self).artifact(notation);
			} else if (self instanceof ConfigurationPublications) {
				((ConfigurationPublications) self).artifact(notation);
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF/* extends ConfigurationVariant | ConfigurationPublications*/> Action<SELF> artifact(Object notation, Action<? super ConfigurablePublishArtifact> action) {
		return self -> {
			if (self instanceof ConfigurationVariant) {
				((ConfigurationVariant) self).artifact(notation, action);
			} else if (self instanceof ConfigurationPublications) {
				((ConfigurationPublications) self).artifact(notation, action);
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF> Function<SELF, PublishArtifact> directory(Project project, Object files, Action<? super ConfigurablePublishArtifact> action) {
		return __ -> {
			final NamedDomainObjectProvider<Sync> syncTask = tasks(project, registerIfAbsent("sync" + StringUtils.capitalize(files.toString()), Sync.class));
			syncTask.configure(task -> {
				task.from(files);
				task.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir(temporaryDir(task))));
			});

			ConfigurablePublishArtifact result = new DefaultConfigurablePublishArtifact(files.toString(), syncTask.map(Sync::getDestinationDir), ArtifactTypeDefinition.DIRECTORY_TYPE);
			action.execute(result);
			return result;
		};
	}

	private static final class DefaultConfigurablePublishArtifact implements ConfigurablePublishArtifact {
		private String name;
		private String extension = "";
		private final Provider<File> file;
		private String type;
		private String classifier = null;
		private final List<Object> builtBy = new ArrayList<>();

		public DefaultConfigurablePublishArtifact(String name, Provider<File> file, String defaultType) {
			this.name = name;
			this.file = file;
			this.type = defaultType;
		}


		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public void setExtension(String extension) {
			this.extension = extension;
		}

		@Override
		public void setType(String type) {
			this.type = type;
		}

		@Override
		public void setClassifier(@Nullable String classifier) {
			this.classifier = classifier;
		}

		@Override
		public ConfigurablePublishArtifact builtBy(Object... tasks) {
			builtBy.addAll(Arrays.asList(tasks));
			return this;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getExtension() {
			return extension;
		}

		@Override
		public String getType() {
			return type;
		}

		@Nullable
		@Override
		public String getClassifier() {
			return classifier;
		}

		@Override
		public File getFile() {
			return file.get();
		}

		@Nullable
		@Override
		public Date getDate() {
			return null;
		}

		@Override
		public TaskDependency getBuildDependencies() {
			return new TaskDependencyInternal() {
				@Override
				public void visitDependencies(TaskDependencyResolveContext context) {
					context.add(file);
					builtBy.forEach(context::add);
				}

				@Override
				public Set<? extends Task> getDependencies(@Nullable Task task) {
					throw new UnsupportedOperationException();
				}
			};
		}
	}

	private static <SELF extends HasConfigurableAttributes<SELF>, T> Action<SELF> attribute(Attribute<T> key, T value) {
		return self -> self.attributes(it -> it.attribute(key, value));
	}

	private static <SELF extends HasConfigurableAttributes<SELF>, T> Action<SELF> attribute(Attribute<T> key, Provider<T> value) {
		return self -> {
			((ConfigurationInternal) self).beforeLocking(__ -> self.attributes(it -> it.attribute(key, value.get())));
		};
	}

	private static Action<Configuration> asDeclarableBucket() {
		return it -> {
			it.setCanBeConsumed(false);
			it.setCanBeResolved(false);
		};
	}

	private static Action<Configuration> asConsumableBucket() {
		return it -> {
			it.setCanBeConsumed(true);
			it.setCanBeResolved(false);
		};
	}

	private static void configurations(Project project, Action<? super ConfigurationContainer> action) {
		action.execute(project.getConfigurations());
	}

	@SuppressWarnings("unchecked")
	private static Action<Configuration> extendsFrom(Object... configurations) {
		return it -> {
			// TODO: flatUnpackUntil(Configuration)
			for (Object configuration : configurations) {
				if (configuration instanceof Provider) {
					ifPresent((Provider<Configuration>) configuration, it::extendsFrom);
				} else {
					it.extendsFrom((Configuration) configuration);
				}
			}
		};
	}

	private static <T> void ifPresent(Provider<T> self, Consumer<? super T> action) {
		final T value = self.getOrNull();
		if (value != null) {
			action.accept(value);
		}
	}

	private static <OUT> OUT configurations(Project project, Function<? super ConfigurationContainer, ? extends OUT> mapper) {
		return mapper.apply(project.getConfigurations());
	}

	private static <SELF extends NamedDomainObjectCollection<E>, E> Function<SELF, NamedDomainObjectProvider<E>> named(String name) {
		return self -> self.named(name);
	}

	private static <SELF extends NamedDomainObjectCollection<E>, E, S extends E> Function<SELF, NamedDomainObjectProvider<S>> named(String name, Class<S> type) {
		return self -> self.named(name, type);
	}

	private static void repositories(Project project, Action<? super RepositoryHandler> action) {
		action.execute(project.getRepositories());
	}

	private static Action<RepositoryHandler> mavenCentral() {
		return RepositoryHandler::mavenCentral;
	}

	private static Action<RepositoryHandler> gradlePluginDevelopment() {
		return repositories -> from(repositories).gradlePluginDevelopment();
	}

	private static Action<RepositoryHandler> mavenLocal() {
		return RepositoryHandler::mavenLocal;
	}

	private static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
		project.getExtensions().configure("sourceSets", action);
	}

	private static <OUT> OUT sourceSets(Project project, Function<? super SourceSetContainer, ? extends OUT> mapper) {
		return mapper.apply((SourceSetContainer) project.getExtensions().getByName("sourceSets"));
	}

	private static void java(Project project, Action<? super JavaPluginExtension> action) {
		project.getExtensions().configure("java", action);
	}

	private static <T extends NamedDomainObjectCollection<E>, E> Action<T> main(Action<? super E> action) {
		return container -> container.named("main", action);
	}

	private static <T extends DomainObjectCollection<E>, E> Action<T> configureEach(Action<? super E> action) {
		return container -> container.configureEach(action);
	}

	private static <SELF/* extends DomainObjectCollection<E> | View<E>*/, E, S extends E> Action<SELF> configureEach(Class<S> type, Action<? super S> action) {
		return container -> {
			if (container instanceof DomainObjectCollection) {
				((DomainObjectCollection<E>) container).withType(type).configureEach(action);
			} else if (container instanceof View) {
				((View<E>) container).configureEach(type, action);
			} else {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static <SELF extends NamedDomainObjectContainer<E>, E> Function<SELF, NamedDomainObjectProvider<E>> register(String name) {
		return container -> container.register(name);
	}

	private static <SELF extends PolymorphicDomainObjectContainer<E>, E, S extends E> Function<SELF, NamedDomainObjectProvider<S>> register(String name, Class<S> type) {
		return container -> container.register(name, type);
	}

	private static <SELF extends PolymorphicDomainObjectContainer<E>, E, S extends E> Function<SELF, NamedDomainObjectProvider<S>> registerIfAbsent(String name, Class<S> type) {
		return container -> {
			if (container.getNames().contains(name)) {
				return container.named(name, type);
			} else {
				return container.register(name, type);
			}
		};
	}

	private static Action<SourceSet> testSourceSets(Action<? super SourceSet> action) {
		return sourceSet -> {
			if (sourceSet.getName().equals("test") || sourceSet.getName().endsWith("Test")) {
				action.execute(sourceSet);
			}
		};
	}

	private static Action<SourceSet> usesLowestJvmCompatibilitySupportedByGradle(Project project, String minimumGradleVersion) {
		return jvmCompatibility(project, minimumJavaVersionFor(minimumGradleVersion));
	}

	private static Action<JavaPluginExtension> usesLowestJvmCompatibilitySupportedByGradle(String minimumGradleVersion) {
		return java -> {
			java.setSourceCompatibility(minimumJavaVersionFor(minimumGradleVersion));
			java.setTargetCompatibility(minimumJavaVersionFor(minimumGradleVersion));
		};
	}

	private static Action<SourceSet> usesLatestJvmCompatibility(Project project) {
		return jvmCompatibility(project, JavaVersion.VERSION_17);
	}

	private static Action<SourceSet> jvmCompatibility(Project project, JavaVersion version) {
		return sourceSet -> {
			// TODO: set compatibility on configuration?
			project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, task -> {
				task.setSourceCompatibility(version.toString());
				task.setTargetCompatibility(version.toString());
				task.getJavaCompiler().set(targetCompatibility(project, sourceSet).flatMap(toJavaCompiler(project)));
			});
		};
	}

	private static class GradlePropertyAdapter<T> implements PropertyAdapter<T> {
		private final Property<T> delegate;

		public GradlePropertyAdapter(Property<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void setFromAnyObject(Object value) {
			((PropertyInternal<?>) delegate).setFromAnyValue(value);
		}

		@Override
		public void conventionFromAnyObject(Object value) {
			// TODO: Should support
			throw new UnsupportedOperationException();
		}
	}
}
