package dev.nokee.docs;

import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import dev.gradleplugins.internal.plugins.SpockFrameworkTestSuiteBasePlugin;
import dev.nokee.docs.tasks.*;
import dev.nokee.docs.types.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.filters.ConcatFilter;
import org.gradle.api.Action;
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
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.util.GUtil;
import org.jbake.gradle.JBakeExtension;
import org.jbake.gradle.JBakeServeTask;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class DocumentationPlugin implements Plugin<Project> {
	private final SourceSetFactory sourceSetFactory = getObjects().newInstance(SourceSetFactory.class);

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

	private LanguageTransform<Asciidoctor, Asciidoctor> processSamplesAsciidoctors(Sample sample, Provider<String> projectVersion) {
		return sourceSet -> {
			String taskName = "process" + sample.getNameAsCamelCase() + "SamplesAsciidoctorsMetadata";
			TaskProvider<ProcessAsciidoctor> processSamplesTask = getTasks().register(taskName, ProcessAsciidoctor.class, task -> {
				task.dependsOn(sourceSet.getSource().getBuiltBy());
				task.getSource().setDir(sourceSet.getSource().getDir()).builtBy(sourceSet.getSource().getBuiltBy()).include(sourceSet.getSource().getIncludes());
				task.getVersion().set(projectVersion);
				task.getMinimumGradleVersion().set("6.2.1");
			});

			return sourceSetFactory.newSourceSet(sourceSet.getName() + "ProcessedSamples", Asciidoctor.class, processSamplesTask, processSamplesTask.flatMap(task -> task.getOutputDirectory()), "**/*.adoc");
		};
	}

	private LanguageTransform<Asciidoctor, Asciicast> compileToAsciicast(Provider<String> projectVersion, Provider<String> documentationVersion, SourceSet<PublicData> dslSourceSet, Sample sample) {
		return sourceSet -> {
			Configuration asciidoctorToAsciinema = getConfigurations().create("asciidoctorToAsciinema" + sample.getNameAsCamelCase());
			asciidoctorToAsciinema.getDependencies().add(getDependencyHandler().create("org.asciidoctor:asciidoctorj-api:2.2.0"));
			asciidoctorToAsciinema.getDependencies().add(getDependencyHandler().create("org.asciidoctor:asciidoctorj:2.2.0"));

			TaskProvider<CreateAsciinema> createAsciinemaTask = getTasks().register("generateSamplesAsciinema" + sample.getNameAsCamelCase(), CreateAsciinema.class, task -> {
				task.dependsOn(sourceSet.getSource(), dslSourceSet.getSource());
				task.getClasspath().from(asciidoctorToAsciinema);
				task.getLocalRepository().set(getLayout().getBuildDirectory().dir("repository"));
				task.getVersion().set(projectVersion);
				task.getRelativePath().set(documentationVersion.map(it -> "docs/" + it + "/samples")); // TODO: Maybe it should be context path instead of relative path

				task.sample(it -> {
					it.getContentFile().fileProvider(getProviders().provider(() -> {
						return sourceSet.getSource().getSingleFile();
					}));
					it.getPermalink().set(sample.getName());
					it.getSource().fileProvider(getProviders().provider(() -> {
						return dslSourceSet.getSource().getDir();
					}));
				});
			});

			SourceSet<Asciicast> result = sourceSetFactory.newSourceSet(sourceSet.getName() + "Asciicast", Asciicast.class, createAsciinemaTask, createAsciinemaTask.flatMap(task -> task.getOutputDirectory()), "**/*.cast");
			return result;
		};
	}

	private SourceSet<GIF> compileToGif(SourceSet<Asciicast> sourceSet) {
		TaskProvider<AsciicastCompile> compileAsciicastTask = getTasks().register("compileDocsAsciicast", AsciicastCompile.class, task -> {
			// Output GIF
			task.dependsOn(sourceSet.getSource().getBuiltBy());
			task.getSource().setDir(sourceSet.getSource().getDir()).builtBy(sourceSet.getSource().getBuiltBy()).include(sourceSet.getSource().getIncludes());
		});

		SourceSet<GIF> result = sourceSetFactory.newSourceSet(sourceSet.getName() + "Gif", GIF.class, compileAsciicastTask, compileAsciicastTask.flatMap(task -> task.getOutputDirectory()), "**/*.gif");
		return result;
	}

	private SourceSet<MP4> compileToMp4(SourceSet<GIF> sourceSet) {
		TaskProvider<GifCompile> compileGifTask = getTasks().register("compileDocsGif", GifCompile.class, task -> {
			// Output MP4
			task.dependsOn(sourceSet.getSource().getBuiltBy());
			task.getSource().setDir(sourceSet.getSource().getDir()).builtBy(sourceSet.getSource().getBuiltBy()).include(sourceSet.getSource().getIncludes());
		});
		return sourceSetFactory.newSourceSet(sourceSet.getName() + "Mp4", MP4.class, compileGifTask, compileGifTask.flatMap(task -> task.getOutputDirectory()), "**/*.mp4");
	}

	private SourceSet<PNG> extractScreenShot(SourceSet<MP4> sourceSet) {
		TaskProvider<ExtractScreenshot> extractScreenshotTask = getTasks().register("extractDocsScreenshot", ExtractScreenshot.class, task -> {
			// Output png
			task.dependsOn(sourceSet.getSource().getBuiltBy());
			task.getSource().setDir(sourceSet.getSource().getDir()).builtBy(sourceSet.getSource().getBuiltBy()).include(sourceSet.getSource().getIncludes());
		});
		return sourceSetFactory.newSourceSet(sourceSet.getName() + "Screenshot", PNG.class, extractScreenshotTask, extractScreenshotTask.flatMap(task -> task.getOutputDirectory()), "**/*.png");
	}

	private SourceSet<HTML> createEmbeddedPlayer(SourceSet<MP4> sourceSet) {
		TaskProvider<CreateEmbeddedPlayer> createEmbeddedPlayer = getTasks().register("createDocsPlayer", CreateEmbeddedPlayer.class, task -> {
			// Output HTML
			task.dependsOn(sourceSet.getSource().getBuiltBy());
			task.getSource().setDir(sourceSet.getSource().getDir()).builtBy(sourceSet.getSource().getBuiltBy()).include(sourceSet.getSource().getIncludes());
		});
		return sourceSetFactory.newSourceSet(sourceSet.getName() + "TwitterPlayer", HTML.class, createEmbeddedPlayer, createEmbeddedPlayer.flatMap(task -> task.getOutputDirectory()), "**/*.html");
	}

	private LanguageTransform<DOT, PNG> compileToPng(Provider<String> documentationVersion) {
		return sourceSet -> {
			TaskProvider<DotCompile> compileDotTask = getTasks().register("compileDocsDot", DotCompile.class, task -> {
				// Output PNG
				task.dependsOn(sourceSet.getSource().getBuiltBy());
				task.getSource().setDir(sourceSet.getSource().getDir()).builtBy(sourceSet.getSource().getBuiltBy()).include(sourceSet.getSource().getIncludes());
				task.getRelativePath().set(documentationVersion.map(it -> "docs/" + it));
			});
			return sourceSetFactory.newSourceSet(sourceSet.getName() + "Png", PNG.class, compileDotTask, compileDotTask.flatMap(task -> task.getOutputDirectory()), "**/*.png");
		};
	}

	SourceSet<PublicData> newGradleWrapperSourceSet(String gradleVersion) {
		String taskName = "generateSamplesGradleWrapper";
		Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/" + taskName);
		TaskProvider<Wrapper> generateSamplesGradleWrapperTask = getTasks().register(taskName, Wrapper.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setGradleVersion(gradleVersion);
			task.setScriptFile(outputDirectory.get().file("gradlew"));
			task.setJarFile(outputDirectory.get().file("gradle/wrapper/gradle-wrapper.jar"));
		});
		return sourceSetFactory.newSourceSet("gradleWrapper", PublicData.class, generateSamplesGradleWrapperTask, outputDirectory, "**/*");
	}

	Provider<FileSystemLocation> kotlinDslSettingsConfiguration(Provider<String> projectVersion) {
		String taskName = "configureKotlinDslSettingsConfiguration";
		Provider<RegularFile> outputFile = getLayout().getBuildDirectory().file("tmp/" + taskName + "/" + Dsl.KOTLIN_DSL.getSettingsFileName());
		TaskProvider<Task> generateKotlinDslNightlySettingsTask = getTasks().register(taskName, task -> {
			task.getInputs().property("version", projectVersion);
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.getOutputs().file(outputFile);
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					try {
						FileUtils.write(outputFile.get().getAsFile(), Dsl.KOTLIN_DSL.getSettingsPluginManagement().withVersion(projectVersion.get()).toString(), Charset.defaultCharset());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
		});
		return getObjects().fileCollection().from(outputFile).builtBy(generateKotlinDslNightlySettingsTask).getElements().map(it -> it.iterator().next());
	}

	private Provider<FileSystemLocation> groovyDslSettingsConfiguration(Provider<String> projectVersion) {
		String taskName = "configureGroovyDslSettingsConfiguration";
		Provider<RegularFile> outputFile = getLayout().getBuildDirectory().file("tmp/" + taskName + "/" + Dsl.GROOVY_DSL.getSettingsFileName());
		TaskProvider<Task> generateKotlinDslNightlySettingsTask = getTasks().register(taskName, task -> {
			task.getInputs().property("version", projectVersion);
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.getOutputs().file(outputFile);
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					try {
						FileUtils.write(outputFile.get().getAsFile(), Dsl.GROOVY_DSL.getSettingsPluginManagement().withVersion(projectVersion.get()).toString(), Charset.defaultCharset());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
		});
		return getObjects().fileCollection().from(outputFile).builtBy(generateKotlinDslNightlySettingsTask).getElements().map(it -> it.iterator().next());
	}

	private LanguageTransform<PublicData, ZIP> zipSample(Sample sample, Dsl dsl, Provider<String> projectVersion) {
		return sourceSet -> {
			TaskProvider<Zip> zipDslSampleTask = getTasks().register("zip" + sample.getNameAsCamelCase() + dsl.getNameAsCamelCase() + "Sample", Zip.class, task -> {
				task.getOutputs().cacheIf(Specs.satisfyAll());
				Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/" + task.getName());
				task.doFirst(new Action<Task>() {
					@Override
					public void execute(Task task) {
						try {
							FileUtils.cleanDirectory(outputDirectory.get().getAsFile());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});

				task.from(sourceSet.getSource());
				task.getArchiveBaseName().set(GUtil.toCamelCase(sample.getName()));
				task.getArchiveClassifier().set(dsl.getName());
				task.getArchiveVersion().set(projectVersion);
				task.getDestinationDirectory().set(outputDirectory);
			});
			return sourceSetFactory.newSourceSet("archive" + sample.getName(), ZIP.class, zipDslSampleTask, zipDslSampleTask.flatMap(it -> it.getDestinationDirectory()), GUtil.toCamelCase(sample.getName()) + "-*-" + dsl.getName() + ".zip");
		};
	}


	private LanguageTransform<PublicData, PublicData> addSettingsInformation(Sample sample, Dsl dsl, Provider<FileSystemLocation> settingsTemplateFile) {
		return sourceSet -> {
			String taskName = "process" + sample.getNameAsCamelCase() + dsl.getNameAsCamelCase() + "SettingsFile";
			Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/" + taskName);
			TaskProvider<Task> processSettingsFileTask = getTasks().register(taskName, task -> {
				task.dependsOn(settingsTemplateFile);
				task.getInputs().file(settingsTemplateFile).withPathSensitivity(PathSensitivity.RELATIVE);
				task.getInputs().files(sourceSet.getSource()).withPathSensitivity(PathSensitivity.RELATIVE);
				task.getOutputs().cacheIf(Specs.satisfyAll());
				task.getOutputs().dir(outputDirectory);
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task task) {
						getFileOperations().sync(copySpec -> {
							copySpec.from(sourceSet.getSource());
							copySpec.from(new File(sourceSet.getSource().getDir(), dsl.getSettingsFileName()), spec -> {
								spec.filter(ImmutableMap.of("prepend", settingsTemplateFile.get().getAsFile()), ConcatFilter.class);
							});
							copySpec.into(outputDirectory);
						});
					}
				});
			});
			return sourceSetFactory.newSourceSet(dsl.getName(), PublicData.class, processSettingsFileTask, outputDirectory, "**/*");
		};
	}

	private Action<Sync> toCombinedSampleSourceSet(Sample sample, Dsl dsl, SourceSet<PublicData>... sourceSets) {
		return task -> {
			task.into(sample.getName() + "/" + dsl.getName(), spec -> {
				Arrays.stream(sourceSets).forEach(sourceSet -> {
					task.getInputs().files(sourceSet.getSource());
					spec.from(sourceSet.getSource());
				});
			});
		};
	}

	private <T extends UTType> SourceSet<T> assemble(String name, SourceSet<T>... sourceSets) {
		if (sourceSets.length < 2) {
			throw new UnsupportedOperationException("Need more source set to combine");
		}
		String taskName = "assemble" + StringUtils.capitalize(name);
		Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/" + taskName);
		TaskProvider<Sync> combineTask = getTasks().register(taskName, Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			Arrays.stream(sourceSets).forEach(sourceSet -> {
				task.getInputs().files(sourceSet.getSource());
				task.from(sourceSet.getSource());
			});
			task.setDestinationDir(outputDirectory.get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});
		return sourceSetFactory.newSourceSet(name, sourceSets[0].getType(), combineTask, outputDirectory, "**/*");
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

		DocumentationExtension extension = project.getExtensions().create("documentation", DocumentationExtension.class, sourceSetFactory, documentationVersion, projectVersion);


		// Staging for all documentation
		TaskProvider<Sync> stageDocumentationTask = tasks.register("stageDocumentation", Sync.class, task -> {
			task.dependsOn("bake");
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(getLayout().getBuildDirectory().dir("generated/baked").get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});
		tasks.named("bakePreview", JBakeServeTask.class, task -> {
			task.setInput(getLayout().getBuildDirectory().dir("generated/baked").get().getAsFile());
		});

		TaskProvider<Task> assembleDocumentationTask = tasks.register("assembleDocumentation", task -> task.dependsOn(stageDocumentationTask));

		Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/assembleAsciicast");
		TaskProvider<Sync> assembleAsciicastTask = tasks.register("assembleAsciicast", Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(outputDirectory.get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});
		SourceSet<Asciicast> allAsciicastSourceSet = sourceSetFactory.newSourceSet("allAsciicastSourceSet", Asciicast.class, assembleAsciicastTask, outputDirectory, "**/*.cast");

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

		TaskProvider<Sync> assembleSampleZipsTask = tasks.register("assembleSampleZips", Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(getLayout().getBuildDirectory().dir("generated/zips").get().getAsFile());
		});

		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		// SAMPLE
		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////
		TaskProvider<Sync> stageSamplesTask = tasks.register("stageSamples", Sync.class, task -> {
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setDestinationDir(getLayout().getBuildDirectory().dir("generated/samples").get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});
		SourceSet<PublicData> allSamplesContent = sourceSetFactory.newSourceSet("allSamplesContent", PublicData.class, stageSamplesTask, getLayout().getBuildDirectory().dir("generated/samples"), "**/*.adoc");
		stageBakeTask.configure(task -> {
			task.getInputs().files(allSamplesContent.getSource());
			task.from(allSamplesContent.getSource(), spec -> spec.into("content/docs/" + documentationVersion.get() + "/samples"));
		});

		TaskProvider<Task> assembleSamplesTask = tasks.register("assembleSamples", task -> {
			task.dependsOn(stageSamplesTask);
		});

		Provider<FileSystemLocation> groovyDslSettingsPrefixFile = groovyDslSettingsConfiguration(projectVersion);
		Provider<FileSystemLocation> kotlinDslSettingsPrefixFile = kotlinDslSettingsConfiguration(projectVersion);
		SourceSet<PublicData> gradleWrapperSourceSet = newGradleWrapperSourceSet("6.2.1");
		extension.getSamples().configureEach(sample -> {
			SourceSet<PublicData> templateSourceSet = sample.getTemplateSourceSet();

			// Groovy DSL
			SourceSet<PublicData> groovyDslSourceSet = assemble(sample.getNameAsLowerCamelCase() + "GroovyDsl", templateSourceSet, gradleWrapperSourceSet, sample.getGroovyDslSourceSet().transform(addSettingsInformation(sample, Dsl.GROOVY_DSL, groovyDslSettingsPrefixFile)));
			stageSamplesTask.configure(toCombinedSampleSourceSet(sample, Dsl.GROOVY_DSL, groovyDslSourceSet));
			SourceSet<ZIP> groovyDslCompressedSourceSet = groovyDslSourceSet.transform(zipSample(sample, Dsl.GROOVY_DSL, projectVersion));
			assembleSamplesTask.configure(it -> it.dependsOn(groovyDslCompressedSourceSet.getSource()));
			assembleSampleZipsTask.configure(task -> {
				task.getInputs().files(groovyDslCompressedSourceSet.getSource());
				task.from(groovyDslCompressedSourceSet.getSource());
			});
			stageDocumentationTask.configure(task -> {
				task.getInputs().files(groovyDslCompressedSourceSet.getSource());
				task.into("docs/" + documentationVersion.get() + "/samples/" + sample.getName(), spec -> spec.from(groovyDslCompressedSourceSet.getSource()));
			});
			stageBakeTask.configure(task -> {
				task.getInputs().files(groovyDslSourceSet.getSource());
				task.from(groovyDslSourceSet.getSource(), spec -> {
					spec.into("content/docs/" + documentationVersion.get() + "/samples/" + sample.getName() +  "/groovy-dsl");
				});
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task t) {
						try {
							new File(task.getDestinationDir(), "content/docs/" + documentationVersion.get() + "/samples/" + sample.getName() + "/groovy-dsl/.jbakeignore").createNewFile();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
			});

			// Kotlin DSL
			SourceSet<PublicData> kotlinDslSourceSet = assemble(sample.getNameAsLowerCamelCase() + "KotlinDsl", templateSourceSet, gradleWrapperSourceSet, sample.getKotlinDslSourceSet().transform(addSettingsInformation(sample, Dsl.KOTLIN_DSL, kotlinDslSettingsPrefixFile)));
			stageSamplesTask.configure(toCombinedSampleSourceSet(sample, Dsl.KOTLIN_DSL, kotlinDslSourceSet));
			SourceSet<ZIP> kotlinDslCompressedSourceSet = kotlinDslSourceSet.transform(zipSample(sample, Dsl.KOTLIN_DSL, projectVersion));
			assembleSamplesTask.configure(it -> it.dependsOn(kotlinDslCompressedSourceSet.getSource()));
			assembleSampleZipsTask.configure(task -> {
				task.getInputs().files(kotlinDslCompressedSourceSet.getSource());
				task.from(kotlinDslCompressedSourceSet.getSource());
			});
			stageDocumentationTask.configure(task -> {
				task.getInputs().files(kotlinDslCompressedSourceSet.getSource());
				task.into("docs/" + documentationVersion.get() + "/samples/" + sample.getName(), spec -> spec.from(kotlinDslCompressedSourceSet.getSource()));
			});
			stageBakeTask.configure(task -> {
				task.getInputs().files(kotlinDslSourceSet.getSource());
				task.from(kotlinDslSourceSet.getSource(), spec -> {
					spec.into("content/docs/" + documentationVersion.get() + "/samples/" + sample.getName() +  "/kotlin-dsl");
				});
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task t) {
						try {
							new File(task.getDestinationDir(), "content/docs/" + documentationVersion.get() + "/samples/" + sample.getName() + "/kotlin-dsl/.jbakeignore").createNewFile();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
			});

			// Content
			SourceSet<Asciidoctor> processedAdocSourceSet = sample.getContentSourceSet().transform(processSamplesAsciidoctors(sample, projectVersion));
			stageSamplesTask.configure(task -> {
				task.getInputs().files(processedAdocSourceSet.getSource());
				task.into(sample.getName(), spec -> spec.from(processedAdocSourceSet.getSource()));
			});

			// Asciinema
			SourceSet<Asciicast> asciicastSourceSet = processedAdocSourceSet.transform(compileToAsciicast(projectVersion, documentationVersion, groovyDslSourceSet, sample));
			assembleAsciicastTask.configure(task -> {
				task.getInputs().files(asciicastSourceSet.getSource());
				task.from(asciicastSourceSet.getSource());
			});
		});

		project.afterEvaluate(proj -> {
			SourceSet<GIF> gifSourceSet = allAsciicastSourceSet.transform(this::compileToGif); // to stage after bake
			SourceSet<MP4> mp4SourceSet = gifSourceSet.transform(this::compileToMp4); // to stage after bake
			SourceSet<PNG> pngSourceSet = mp4SourceSet.transform(this::extractScreenShot); // to stage after bake
			SourceSet<HTML> htmlSourceSet = mp4SourceSet.transform(this::createEmbeddedPlayer); // to stage after bake

			stageDocumentationTask.configure(task -> {
				task.getInputs().files(gifSourceSet.getSource());
				task.getInputs().files(mp4SourceSet.getSource());
				task.getInputs().files(pngSourceSet.getSource());
				task.getInputs().files(htmlSourceSet.getSource());

				task.from(gifSourceSet.getSource());
				task.from(mp4SourceSet.getSource());
				task.from(pngSourceSet.getSource());
				task.from(htmlSourceSet.getSource());
			});
		});


		SourceSet<Asciidoctor> contentSourceSet = extension.getContentSourceSet();
		SourceSet<PublicData> jbakeAssetsSourceSet = sourceSetFactory.newSourceSet("jbakeAssets", PublicData.class);
		jbakeAssetsSourceSet.getSource().setDir("src/jbake/assets").include("**/*");
		SourceSet<PublicData> jbakeTemplatesSourceSet = sourceSetFactory.newSourceSet("jbakeTemplates", PublicData.class);
		jbakeTemplatesSourceSet.getSource().setDir("src/jbake/templates").include("**/*");
		SourceSet<PublicData> jbakeContentSourceSet = sourceSetFactory.newSourceSet("jbakeContent", PublicData.class);
		jbakeContentSourceSet.getSource().setDir("src/jbake/content").include("**/*");
		stageBakeTask.configure(task -> {
			task.getInputs().files(contentSourceSet.getSource());
			task.getInputs().files(jbakeContentSourceSet.getSource());
			task.getInputs().files(jbakeContentSourceSet.getSource());
			task.getInputs().files(jbakeTemplatesSourceSet.getSource());
			task.getInputs().file("src/jbake/jbake.properties");

			task.from(contentSourceSet.getSource(), spec -> spec.into("content"));
			task.from(jbakeContentSourceSet.getSource(), spec -> spec.into("content"));
			task.from(jbakeAssetsSourceSet.getSource(), spec -> spec.into("assets"));
			task.from(jbakeTemplatesSourceSet.getSource(), spec -> spec.into("templates"));
			task.from("src/jbake/jbake.properties");
		});

		// *.dot -> *.png
		SourceSet<DOT> dotSourceSet = sourceSetFactory.newSourceSet("docsDot", DOT.class);
		dotSourceSet.getSource().setDir("src/docs").include("**/*.dot");
		SourceSet<PNG> dotAsPngSourceSet = dotSourceSet.transform(compileToPng(documentationVersion));
		stageDocumentationTask.configure(task -> {
			task.getInputs().files(dotAsPngSourceSet.getSource());
			task.from(dotAsPngSourceSet.getSource());
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

			task.from(content, spec -> spec.into("content"));
			task.from(assets, spec -> spec.into("assets"));
			task.from(templates, spec -> spec.into("templates"));
		});

		// Configurations (outgoing)
		configurations.create("contentElements", configuration -> {
			configuration.setCanBeResolved(false);
			configuration.setCanBeConsumed(true);
			configuration.attributes(attributes -> {
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "jbake-content"));
			});
//			project.afterEvaluate(proj -> {
//				components.withType(JBakeContentSourceSet.class).stream().map(LanguageSourceSet::getSource).forEach(source -> {
//					source.getFiles().forEach(file -> {
//						configuration.getOutgoing().artifact(file, it -> it.builtBy(source));
//					});
//				});
//			});
		});
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
			configuration.getOutgoing().getVariants().maybeCreate("directory").artifact(jbakeTemplatesSourceSet.getSource().getDir(), it -> {
				it.setType(ArtifactTypeDefinition.DIRECTORY_TYPE);
				it.builtBy(jbakeTemplatesSourceSet.getSource());
			});
		});
		TaskProvider<Zip> zipJbakeTemplatesTask = tasks.register("zipJbakeTemplates", Zip.class, task ->{
			task.from(jbakeTemplatesSourceSet.getSource());
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
