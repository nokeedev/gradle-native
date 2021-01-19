package dev.gradleplugins.documentationkit.internal;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dev.gradleplugins.documentationkit.*;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeAction;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.*;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;
import static dev.nokee.utils.TransformerUtils.transformEach;

// add artifact to API reference component
public class JavadocRenderPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().withPlugin("dev.gradleplugins.documentation.api-reference", appliedPlugin -> {
			val components = project.getExtensions().getByType(ComponentContainer.class);
			val manifestFactory = new ManifestFactory(project.getDependencies());
			val configurations = project.getConfigurations();

			val lookup = project.getExtensions().getByType(ModelLookup.class);
			val apiReference = lookup.get(path("components.apiReference"));
			val taskRegistry = new TaskRegistry(project.getTasks(), TaskNamingScheme.forComponent("apiReference"));
			val javadocFactory = new JavadocApiReferenceNodeRegistrationFactory(project.getObjects());
			val javadocArtifact = apiReference.register(javadocFactory.create("javadoc"));
			ModelNodes.of(javadocArtifact).applyTo(onJavadocArtifactDiscovered(taskRegistry, project.getLayout()));

			components.configure("apiReference", ApiReferenceDocumentation.class, component -> {
				javadocArtifact.configure(javadoc -> {
					javadoc.getSources().from(thisManifestArtifact(component));
					javadoc.getSources().from(sourcesOfOtherManifestArtifact(component));
					javadoc.getClasspath().from(classpathOfOtherManifestArtifact(component, manifestFactory, configurations));
					javadoc.getClasspath().from(compileClasspathIfAvailable(configurations));

					// Configuration...
					javadoc.getPermalink().set("javadoc");
					javadoc.getLinks().set(ImmutableList.of(project.uri("https://docs.oracle.com/javase/8/docs/api"), project.uri("https://docs.gradle.org/" + project.property("minimumGradleVersion") + "/javadoc/")));
				});
			});
		});
	}

	private static Object thisManifestArtifact(ApiReferenceDocumentation component) {
		return component.getManifest().getDestinationLocation().dir("sources");
	}

	private static Object sourcesOfOtherManifestArtifact(ApiReferenceDocumentation component) {
		return component.getDependencies().getManifest().getAsLenientFileCollection().getElements().map(transformEach(it -> new File(it.getAsFile(), "sources")));
	}

	private static Callable<Object> classpathOfOtherManifestArtifact(ApiReferenceDocumentation component, ManifestFactory manifestFactory, ConfigurationContainer configurations) {
		return memoizeCallable(() -> component.getDependencies().getManifest().getAsLenientFileCollection().getElements()
			.map(transformEach(manifestFactory::create))
			.map(flatTransformEach(Manifest::getDependencies))
			.map(toDetachedConfiguration(configurations)));
	}

	private static Transformer<Object, List<Dependency>> toDetachedConfiguration(ConfigurationContainer configurations) {
		return it -> configurations.detachedConfiguration(it.toArray(new Dependency[0])).getIncoming().getFiles();
	}

	private static <T> Callable<T> memoizeCallable(Callable<T> callable) {
		return Suppliers.memoize(new Supplier<T>() {
			@Override
			@SneakyThrows
			public T get() {
				return callable.call();
			}
		})::get;
	}

	private static Callable<Object> compileClasspathIfAvailable(ConfigurationContainer configurations) {
		return () -> {
			val compileClasspath = configurations.findByName("compileClasspath");
			if (compileClasspath == null) {
				return ImmutableList.of();
			}
			return compileClasspath;
		};
	}

	private static final class ManifestFactory {
		private final DependencyHandler dependencyFactory;

		ManifestFactory(DependencyHandler dependencyFactory) {
			this.dependencyFactory = dependencyFactory;
		}

		public Manifest create(FileSystemLocation location) {
			return new Manifest(location, dependencyFactory);
		}
	}

	@ToString
	private static final class Manifest {
		private final File location;
		private final DependencyHandler dependencyFactory;

		Manifest(FileSystemLocation location, DependencyHandler dependencyFactory) {
			this.location = location.getAsFile();
			this.dependencyFactory = dependencyFactory;
		}

		public List<Dependency> getDependencies() {
			try {
				val gson = new GsonBuilder().registerTypeAdapter(Dependency.class, new DependencyDeserializer()).create();
				return gson.fromJson(getDependenciesManifestContent(), new TypeToken<List<Dependency>>() {}.getType());
			} catch (IOException e) {
				return Collections.emptyList();
			}
		}

		private String getDependenciesManifestContent() throws IOException {
			return FileUtils.readFileToString(getDependenciesManifestFile(), StandardCharsets.UTF_8);
		}

		private File getDependenciesManifestFile() {
			return new File(location, "dependencies.manifest");
		}

		private class DependencyDeserializer implements JsonDeserializer<Dependency> {
			public Dependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				val obj = json.getAsJsonObject();
				return dependencyFactory.create(String.format("%s:%s:%s", obj.get("group").getAsString(), obj.get("name").getAsString(), obj.get("version").getAsString()));
			}
		}
	}

	private static NodeAction onJavadocArtifactDiscovered(TaskRegistry taskRegistry, ProjectLayout projectLayout) {
		return self(discover(context -> {
				val artifact = context.projectionOf(of(JavadocApiReference.class));
				val javadocTask = taskRegistry.register(TaskName.of("generate", "javadoc"), Javadoc.class, new Consumer<Javadoc>() {
					@Override
					public void accept(Javadoc task) {
						val temporaryDirectory = projectLayout.getBuildDirectory().dir("tmp/" + task.getName());

						task.getInputs().files(artifact.flatMap(it -> it.getSources().getAsFileTree().getElements()));
						task.source(fromDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcepathJavadocOption(temporaryDirectory.get()));
						task.setDestinationDir(temporaryDirectory.get().dir("docs").getAsFile());
						StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) task.getOptions();
						options.setEncoding("utf-8");
						options.setDocEncoding("utf-8");
						options.setCharSet("utf-8");
						options.setLinks(linksFromArtifact());
						options.setClasspath(classpathFromArtifact());
						options.addStringsOption("sourcepath").setValue(sourcePathsFromArtifactSources());
						options.addStringsOption("subpackages").setValue(guessSubPackages());
						options.addStringsOption("exclude").setValue(excludesInternalPackages());
					}

					@SneakyThrows
					private File fromDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcepathJavadocOption(Directory temporaryDirectory) {
						val result = temporaryDirectory.file("Foo.java").getAsFile();
						FileUtils.write(result, "package internal;\n class Foo {}\n", StandardCharsets.UTF_8);
						return result;
					}

					private List<String> linksFromArtifact() {
						return artifact.flatMap(JavadocApiReference::getLinks).map(it -> it.stream().map(URI::toString).collect(Collectors.toList())).get();
					}

					private List<File> classpathFromArtifact() {
						return ImmutableList.copyOf(artifact.map(JavadocApiReference::getClasspath).get());
					}

					private List<String> sourcePathsFromArtifactSources() {
						return artifact.map(JavadocApiReference::getSources).get().getSourceDirectories().getFiles().stream().map(File::getAbsolutePath).collect(Collectors.toList());
					}

					private List<String> guessSubPackages() {
						val result = new HashSet<String>();
						artifact.map(JavadocApiReference::getSources).get().getAsFileTree().visit(new FileVisitor() {
							@Override
							public void visitDir(FileVisitDetails details) {
								result.add(details.getRelativePath().getSegments()[0]);
								details.stopVisiting();
							}

							@Override
							public void visitFile(FileVisitDetails details) {
								// ignore
							}
						});
						return ImmutableList.copyOf(result);
					}

					private List<String> excludesInternalPackages() {
						val packageToExcludes = new ArrayList<String>();
						artifact.map(JavadocApiReference::getSources).get().getAsFileTree().visit(new FileVisitor() {
							@Override
							public void visitDir(FileVisitDetails details) {
								if (details.getName().equals("internal")) {
									packageToExcludes.add(toPackage(details.getRelativePath()));
								}
							}

							private String toPackage(RelativePath path) {
								return String.join(".", path.getSegments());
							}

							@Override
							public void visitFile(FileVisitDetails details) {
								// ignore
							}
						});
						return packageToExcludes;
					}
				});

				val syncTask = taskRegistry.register(TaskName.of("assemble", "javadoc"), Sync.class, task -> {
					task.from(javadocTask.map(Javadoc::getDestinationDir), spec -> spec.into(artifact.flatMap(JavadocApiReference::getPermalink)));
					task.setDestinationDir(projectLayout.getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
				});
				artifact.configure(it -> it.getDestinationDirectory().fileProvider(syncTask.map(Sync::getDestinationDir)).disallowChanges());

				val assembleTask = taskRegistry.register("assemble", Task.class, task -> {
					task.dependsOn(syncTask);
					task.setGroup("documentation");
				});
			}));
	}
}
