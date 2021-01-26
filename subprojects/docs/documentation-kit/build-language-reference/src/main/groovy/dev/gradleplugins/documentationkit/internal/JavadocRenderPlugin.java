package dev.gradleplugins.documentationkit.internal;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.gradleplugins.documentationkit.*;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.NodeAction;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerUtils.*;
import static java.lang.String.join;

// add artifact to API reference component
public class JavadocRenderPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().withPlugin("dev.gradleplugins.documentation.api-reference", appliedPlugin -> {
			val components = project.getExtensions().getByType(ComponentContainer.class);
			val configurations = project.getConfigurations();

			val lookup = project.getExtensions().getByType(ModelLookup.class);
			val apiReference = lookup.get(path("components.apiReference"));
			val taskRegistry = new TaskRegistry(project.getTasks(), TaskNamingScheme.forComponent("apiReference"));
			val javadocFactory = new JavadocApiReferenceNodeRegistrationFactory(project.getObjects());
			val javadocArtifact = apiReference.register(javadocFactory.create("javadoc"));
			ModelNodes.of(javadocArtifact).applyTo(onJavadocArtifactDiscovered(taskRegistry, project.getLayout(), project.getObjects()));

			components.configure("apiReference", ApiReferenceDocumentation.class, component -> {
				javadocArtifact.configure(javadoc -> {
					javadoc.getSources().from(thisManifestArtifact(component));
					javadoc.getSources().from(sourcesOfOtherManifestArtifact(component));
					javadoc.getClasspath().from(classpathOfOtherManifestArtifact(component, DependenciesLoader.forProject(project)));
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

	private static Callable<Object> classpathOfOtherManifestArtifact(ApiReferenceDocumentation component, DependenciesLoader dependencyLoader) {
		return memoizeCallable(() -> component.getDependencies().getManifest().getAsLenientFileCollection().getElements()
			.map(transformEach(it -> new File(it.getAsFile(), "dependencies.manifest")))
			.map(transformEach(dependencyLoader::load)));
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

	private static NodeAction onJavadocArtifactDiscovered(TaskRegistry taskRegistry, ProjectLayout projectLayout, ObjectFactory objects) {
		return self(discover(context -> {
			val artifact = context.projectionOf(of(JavadocApiReference.class));
			val javadocTask = taskRegistry.register(TaskName.of("generate", "javadoc"), Javadoc.class, new Consumer<Javadoc>() {
				@Override
				public void accept(Javadoc task) {
					val temporaryDirectory = projectLayout.getBuildDirectory().dir("tmp/" + task.getName());

					val sources = artifact.flatMap(it -> it.getSources().getAsFileTree().getElements());
					task.getInputs().files(sources);
					task.source(sources.map(ifNonEmpty(forSupplier(fromDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcepathJavadocOption(temporaryDirectory.get())))));
					task.setDestinationDir(temporaryDirectory.get().dir("docs").getAsFile());
					task.setClasspath(objects.fileCollection().from(classpathFromArtifact()));
					StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) task.getOptions();
					options.setEncoding("utf-8");
					options.setDocEncoding("utf-8");
					options.setCharSet("utf-8");

					// Delay the realization while keeping task dependencies
					val args = objects.listProperty(String.class);
					args.addAll(linksFromArtifact().map(flatTransformEach(it -> ImmutableList.of("-link", it))));
					args.addAll(sourcePathsFromArtifactSources().map(ifNonEmpty(it -> ImmutableList.of("-sourcepath", join(File.pathSeparator, it)))));
					args.addAll(guessSubPackages().map(ifNonEmpty(it -> ImmutableList.of("-subpackages", join(File.pathSeparator, it)))));
					args.addAll(excludesInternalPackages().map(ifNonEmpty(it -> ImmutableList.of("-exclude", join(File.pathSeparator, it)))));
					args.addAll(artifact.flatMap(JavadocApiReference::getTitle).map(it -> ImmutableList.of("-doctitle", quote(it), "-windowtitle", quote(it))).orElse(ImmutableList.of()));
					task.getInputs().property("additionalArgs", args);
					task.doFirst(t -> {
						try {
							FileUtils.write(temporaryDirectory.get().file("additionalArgs.options").getAsFile(), join(System.lineSeparator(), args.get()), StandardCharsets.UTF_8);
							options.optionFiles(temporaryDirectory.get().file("additionalArgs.options").getAsFile());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				}

				private java.util.function.Supplier<Iterable<File>> fromDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcepathJavadocOption(Directory temporaryDirectory) {
					return new java.util.function.Supplier<Iterable<File>>() {
						@Override
						@SneakyThrows
						public Iterable<File> get() {
							val result = temporaryDirectory.file("Foo.java").getAsFile();
							FileUtils.write(result, "package internal;\n class Foo {}\n", StandardCharsets.UTF_8);
							return ImmutableList.of(result);
						}
					};
				}

				private Provider<List<String>> linksFromArtifact() {
					return artifact.flatMap(JavadocApiReference::getLinks).map(transformEach(URI::toString));
				}

				private Provider<Set<FileSystemLocation>> classpathFromArtifact() {
					return artifact.flatMap(elementsOf(JavadocApiReference::getClasspath));
				}

				private Provider<List<String>> sourcePathsFromArtifactSources() {
					return artifact.map(JavadocApiReference::getSources).flatMap(elementsOf(LanguageSourceSet::getSourceDirectories)).map(transformEach(asFile(File::getAbsolutePath)));
				}

				private Provider<List<String>> guessSubPackages() {
					return artifact.map(JavadocApiReference::getSources).map(LanguageSourceSet::getAsFileTree).map(files -> {
						val visitor = new GuessSubPackageVisitor();
						files.visit(visitor);
						return ImmutableList.copyOf(visitor.result);
					});
				}

				private Provider<List<String>> excludesInternalPackages() {
					return artifact.map(JavadocApiReference::getSources).map(LanguageSourceSet::getAsFileTree).map(files -> {
						val visitor = new ExcludesInternalPackages();
						files.visit(visitor);
						return visitor.packageToExcludes;
					});
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

	private static final class GuessSubPackageVisitor implements FileVisitor {
		private final List<String> result = new ArrayList<>();

		@Override
		public void visitDir(FileVisitDetails details) {
			result.add(details.getRelativePath().getSegments()[0]);
			details.stopVisiting();
		}

		@Override
		public void visitFile(FileVisitDetails details) {
			// ignore
		}
	}

	private static final class ExcludesInternalPackages implements FileVisitor {
		private final List<String> packageToExcludes = new ArrayList<>();

		@Override
		public void visitDir(FileVisitDetails details) {
			if (details.getName().equals("internal")) {
				packageToExcludes.add(toPackage(details.getRelativePath()));
			}
		}

		private String toPackage(RelativePath path) {
			return join(".", path.getSegments());
		}

		@Override
		public void visitFile(FileVisitDetails details) {
			// ignore
		}
	}

	private static String quote(String valueToQuote) {
		return "\"" + valueToQuote + "\"";
	}

	private static <OUT, IN extends FileSystemLocation> Transformer<OUT, IN> asFile(Function<File, ? extends OUT> mapper) {
		return new Transformer<OUT, IN>() {
			@Override
			public OUT transform(IN in) {
				return mapper.apply(in.getAsFile());
			}
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

	private static <OUT, IN extends Iterable<T>, T> Transformer<Iterable<OUT>, IN> ifNonEmpty(Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
		return new Transformer<Iterable<OUT>, IN>() {
			@Override
			public Iterable<OUT> transform(IN ts) {
				if (Iterables.isEmpty(ts)) {
					return ImmutableList.of();
				}
				return mapper.transform(ts);
			}
		};
	}
}
