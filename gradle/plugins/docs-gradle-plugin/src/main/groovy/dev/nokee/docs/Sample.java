package dev.nokee.docs;

import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.nokee.docs.tasks.*;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.filters.ConcatFilter;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.wrapper.Wrapper;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public abstract class Sample implements Named {
	private final String name;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencies;

	@Inject
	public Sample(String name, ProjectLayout layout, TaskContainer tasks, DependencyHandler dependencies) {
		this.name = name;
		this.dependencies = dependencies;

		// Configure template source set
		TaskProvider<GenerateSampleContent> generateSampleCodeTask = tasks.register(getTaskName("generate", "content"), GenerateSampleContent.class, task -> {
			task.getTemplate().value(getTemplate()).disallowChanges();
		});
		getTemplateSourceSet().from(generateSampleCodeTask.flatMap(GenerateSampleContent::getOutputDirectory));

		// Gradle Wrapper
		Provider<Directory> gradleWrapperFiles = generateGradleWrapper();

		// Configure Groovy DSL
		Provider<FileSystemLocation> groovyDslSettingsPrefixFile = groovyDslSettingsConfiguration(getProductVersion());
		getGroovyDslSourceSet().from(addSettingsInformation(Dsl.GROOVY_DSL, groovyDslSettingsPrefixFile));
		getGroovyDslSourceSet().from(layout.getProjectDirectory().dir("src/docs/samples/" + getName() + "/" + Dsl.GROOVY_DSL.getName()).getAsFileTree());
		getGroovyDslSourceSet().from(gradleWrapperFiles);
		getGroovyDslSourceSet().from(getTemplateSourceSet());
		Provider<RegularFile> groovyDslZip = zipSample(getGroovyDslSourceSet(), Dsl.GROOVY_DSL);
		getArchiveSourceSet().from(groovyDslZip);

		// Configure Kotlin DSL
		Provider<FileSystemLocation> kotlinDslSettingsPrefixFile = kotlinDslSettingsConfiguration(getProductVersion());
		getKotlinDslSourceSet().from(addSettingsInformation(Dsl.KOTLIN_DSL, kotlinDslSettingsPrefixFile));
		getKotlinDslSourceSet().from(layout.getProjectDirectory().dir("src/docs/samples/" + getName() + "/" + Dsl.KOTLIN_DSL.getName()).getAsFileTree());
		getKotlinDslSourceSet().from(gradleWrapperFiles);
		getKotlinDslSourceSet().from(getTemplateSourceSet());
		Provider<RegularFile> kotlinDslZip = zipSample(getKotlinDslSourceSet(), Dsl.KOTLIN_DSL);
		getArchiveSourceSet().from(kotlinDslZip);

		// Configure Asciidoctor content
		getAttributes().empty();
		getAttributes().put("jbake-version", getProductVersion());
		getAttributes().put("toc", "");
		getAttributes().put("toclevels", "1");
		getAttributes().put("toc-title", "Contents");
		getAttributes().put("icons", "font");
		getAttributes().put("idprefix", "");
		getAttributes().put("jbake-status", "published");
		getAttributes().put("encoding", "utf-8");
		getAttributes().put("lang", "en-US");
		getAttributes().put("sectanchors", "true");
		getAttributes().put("sectlinks", "true");
		getAttributes().put("linkattrs", "true");
		getAttributes().put("gradle-user-manual", getMinimumGradleVersion().map(version -> "https://docs.gradle.org/" + version + "/userguide"));
		getAttributes().put("gradle-language-reference", getMinimumGradleVersion().map(version -> "https://docs.gradle.org/" + version + "/dsl"));
		getAttributes().put("gradle-api-reference", getMinimumGradleVersion().map(version -> "https://docs.gradle.org/" + version + "/javadoc"));
		getAttributes().put("gradle-guides", "https://guides.gradle.org/");
		getAttributes().put("jbake-permalink", getName());
		getAttributes().put("jbake-archivebasename", GUtil.toCamelCase(getName()));
		getAttributes().put("includedir", ".");
		TaskProvider<ProcessAsciidoctor> generateHeaderTask = tasks.register(getTaskName("process", "asciidoctor"), ProcessAsciidoctor.class, task -> {
			task.getSource().setDir("src/docs/samples/" + getName()).include("README.adoc");
			task.getAttributes().set(getAttributes());
		});
		getContentSourceSet().from(generateHeaderTask.flatMap(ProcessAsciidoctor::getOutputDirectory));
		getContentSourceSet().from(compileDotToPng());
		Provider<RegularFile> contentFile = generateHeaderTask.flatMap(it -> it.getOutputDirectory().map(dir -> dir.file("README.adoc")));

		// Generate Asciinema files
		Provider<RegularFile> asciicastFile = generateAsciicastFile(contentFile);
		Provider<RegularFile> gifFile = compileAsciicastToGif(asciicastFile);
		Provider<RegularFile> mp4File = compileGifToMp4(gifFile);
		Provider<RegularFile> screenShotFile = extractScreenShot(mp4File);
		Provider<RegularFile> htmlPlayerFile = createEmbeddedPlayer(mp4File);
		getAsciinemaSourceSet().from(asciicastFile).from(gifFile).from(mp4File).from(screenShotFile).from(htmlPlayerFile);

		TaskProvider<StageSample> stageTask = tasks.register(getTaskName("stage"), StageSample.class, task -> {
			task.getContentSources().from(groovyDslZip);
			task.getContentSources().from(kotlinDslZip);
			task.getContentSources().from(getContentSourceSet());
			task.getContentSources().from(getObjects().fileTree().setDir("src/docs/samples/" + getName()).include("**/*.png"));
			task.getContentSources().from(getObjects().fileTree().setDir("src/docs/samples/" + getName()).include("**/*.gif"));
			task.getGroovyDslSources().from(getGroovyDslSourceSet());
			task.getKotlinDslSources().from(getKotlinDslSourceSet());
		});
		getStageSourceSet().from(stageTask.flatMap(StageSample::getDestinationDirectory));
	}

	private String getTaskName(String verb) {
		return verb + getNameAsCamelCase() + "Sample";
	}

	private String getTaskName(String verb, String suffix) {
		return verb + getNameAsCamelCase() + "Sample" + StringUtils.capitalize(suffix);
	}

	private Provider<FileSystemLocation> addSettingsInformation(Dsl dsl, Provider<FileSystemLocation> settingsTemplateFile) {
		String taskName = "process" + getNameAsCamelCase() + dsl.getNameAsCamelCase() + "SettingsFile";
		Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/" + taskName);
		RegularFile oldSettingsFile = getLayout().getProjectDirectory().file("src/docs/samples/" + getName() + "/" + dsl.getName() + "/" + dsl.getSettingsFileName());
		TaskProvider<Task> processSettingsFileTask = getTasks().register(taskName, task -> {
			task.dependsOn(settingsTemplateFile);
			task.getInputs().file(settingsTemplateFile).withPathSensitivity(PathSensitivity.RELATIVE);
			task.getInputs().file(oldSettingsFile).withPathSensitivity(PathSensitivity.RELATIVE);
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.getOutputs().dir(outputDirectory);
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					getFileOperations().sync(copySpec -> {
						copySpec.from(oldSettingsFile, spec -> {
							spec.filter(ImmutableMap.of("prepend", settingsTemplateFile.get().getAsFile()), ConcatFilter.class);
						});
						copySpec.into(outputDirectory);
//						copySpec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
					});
				}
			});
		});
		return getObjects().fileCollection().from(outputDirectory).builtBy(processSettingsFileTask).getAsFileTree().getElements().map(it -> it.iterator().next());
	}

	Provider<FileSystemLocation> kotlinDslSettingsConfiguration(Provider<String> projectVersion) {
		String taskName = "configureKotlinDslSettingsConfiguration";
		Provider<RegularFile> outputFile = getLayout().getBuildDirectory().file("tmp/" + taskName + "/" + Dsl.KOTLIN_DSL.getSettingsFileName());
		TaskProvider<Task> generateKotlinDslNightlySettingsTask = null;
		if (getTasks().getNames().contains(taskName)) {
			generateKotlinDslNightlySettingsTask = getTasks().named(taskName);
		} else {
			generateKotlinDslNightlySettingsTask = getTasks().register(taskName, task -> {
				task.getInputs().property("version", projectVersion);
				task.getOutputs().cacheIf(Specs.satisfyAll());
				task.getOutputs().file(outputFile);
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task task) {
						try {
							FileUtils.write(outputFile.get().getAsFile(), getPluginManagementBlock().get().asKotlinDsl().toString(), Charset.defaultCharset());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
			});
		}
		return getObjects().fileCollection().from(outputFile).builtBy(generateKotlinDslNightlySettingsTask).getElements().map(it -> it.iterator().next());
	}

	private Provider<FileSystemLocation> groovyDslSettingsConfiguration(Provider<String> projectVersion) {
		String taskName = "configureGroovyDslSettingsConfiguration";
		Provider<RegularFile> outputFile = getLayout().getBuildDirectory().file("tmp/" + taskName + "/" + Dsl.GROOVY_DSL.getSettingsFileName());
		TaskProvider<Task> generateGroovyDslNightlySettingsTask = null;
		if (getTasks().getNames().contains(taskName)) {
			generateGroovyDslNightlySettingsTask = getTasks().named(taskName);
		} else {
			generateGroovyDslNightlySettingsTask = getTasks().register(taskName, task -> {
				task.getInputs().property("blockClass", getPluginManagementBlock().get().getClass().getCanonicalName());
				task.getInputs().property("version", projectVersion);
				task.getOutputs().cacheIf(Specs.satisfyAll());
				task.getOutputs().file(outputFile);
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task task) {
						try {
							FileUtils.write(outputFile.get().getAsFile(), getPluginManagementBlock().get().asGroovyDsl().toString(), Charset.defaultCharset());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
			});
		}
		return getObjects().fileCollection().from(outputFile).builtBy(generateGroovyDslNightlySettingsTask).getElements().map(it -> it.iterator().next());
	}

	private Provider<RegularFile> generateAsciicastFile(Provider<RegularFile> contentFile) {
		Configuration asciidoctorToAsciinema = getConfigurations().create("asciidoctorToAsciinema" + getNameAsCamelCase());
		asciidoctorToAsciinema.getDependencies().add(getDependencies().create("org.asciidoctor:asciidoctorj-api:2.2.0"));
		asciidoctorToAsciinema.getDependencies().add(getDependencies().create("org.asciidoctor:asciidoctorj:2.2.0"));

		TaskProvider<CreateAsciinema> createAsciinemaTask = getTasks().register(getTaskName("generate", "asciinema"), CreateAsciinema.class, task -> {
			task.dependsOn(getContentSourceSet(), getGroovyDslSourceSet());
			task.getClasspath().from(asciidoctorToAsciinema);
			// TODO: Fix the evil` in the following code
			task.getLocalRepository().set(getLayout().getProjectDirectory().dir("../distributions/build/repository"));
			task.getVersion().set(getProductVersion());
			task.getPluginManagementBlock().convention(getPluginManagementBlock().map(it -> it.withRepository(getLayout().getProjectDirectory().dir("../distributions/build/repository").getAsFile().getAbsolutePath())));

			task.getContentFile().set(contentFile);
			task.getSource().from(getGroovyDslSourceSet());
		});

		return createAsciinemaTask.flatMap(CreateAsciinema::getAsciicastFile);
	}

	private Provider<RegularFile> compileAsciicastToGif(Provider<RegularFile> file) {
		TaskProvider<AsciicastCompile> compileAsciicastTask = getTasks().register(getTaskName("compile", "asciicast"), AsciicastCompile.class, task -> {
			task.getAsciicastFile().set(file);
		});

		return compileAsciicastTask.flatMap(AsciicastCompile::getGifVideoFile);
	}

	private Provider<RegularFile> compileGifToMp4(Provider<RegularFile> file) {
		TaskProvider<GifCompile> compileGifTask = getTasks().register(getTaskName("compile", "gif"), GifCompile.class, task -> {
			task.getGifVideoFile().set(file);
		});
		return compileGifTask.flatMap(GifCompile::getMp4VideoFile);
	}

	private Provider<RegularFile> extractScreenShot(Provider<RegularFile> file) {
		TaskProvider<ExtractScreenshot> extractScreenshotTask = getTasks().register(getTaskName("extract", "screenshot"), ExtractScreenshot.class, task -> {
			task.getMp4VideoFile().set(file);
		});
		return extractScreenshotTask.flatMap(ExtractScreenshot::getScreenshotFile);
	}

	private Provider<RegularFile> createEmbeddedPlayer(Provider<RegularFile> file) {
		TaskProvider<CreateEmbeddedPlayer> createEmbeddedPlayer = getTasks().register(getTaskName("create", "embeddedPlayer"), CreateEmbeddedPlayer.class, task -> {
			task.getMp4FileName().set(file.map(it -> it.getAsFile().getName()));
		});
		return createEmbeddedPlayer.flatMap(CreateEmbeddedPlayer::getHtmlPlayerFile);
	}

	private Provider<Directory> compileDotToPng() {
		TaskProvider<DotCompile> compileDotTask = getTasks().register("compileDocsDot" + getNameAsCamelCase(), DotCompile.class, task -> {
			task.getSource().setDir("src/docs/samples/" + getName()).include("**/*.dot");
		});
		return compileDotTask.flatMap(DotCompile::getOutputDirectory);
	}

	private Provider<RegularFile> zipSample(FileCollection source, Dsl dsl) {
		TaskProvider<Zip> zipDslSampleTask = getTasks().register(getTaskName("zip", dsl.getNameAsCamelCase()), Zip.class, task -> {
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

			task.from(source);
			task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
			task.getArchiveBaseName().set(GUtil.toCamelCase(getName()));
			task.getArchiveClassifier().set(dsl.getName());
			task.getArchiveVersion().set(getProductVersion());
			task.getDestinationDirectory().set(outputDirectory);
		});
		return zipDslSampleTask.flatMap(Zip::getArchiveFile);
	}

	private Provider<Directory> generateGradleWrapper() {
		TaskProvider<Wrapper> gradleWrapperTask = getTasks().register("generate" + getNameAsCamelCase() + "SampleGradleWrapper", Wrapper.class, task -> {
			Provider<Directory> outputDirectory = getLayout().getBuildDirectory().dir("tmp/" + task.getName());
			task.getOutputs().cacheIf(Specs.satisfyAll());
			task.setGradleVersion(getMinimumGradleVersion().get());
			task.setScriptFile(outputDirectory.get().file("gradlew"));
			task.setJarFile(outputDirectory.get().file("gradle/wrapper/gradle-wrapper.jar"));
		});
		return getObjects().directoryProperty().fileProvider(gradleWrapperTask.map(it -> it.getScriptFile().getParentFile()));
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

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

	public abstract Property<String> getProductVersion();

	public abstract Property<String> getMinimumGradleVersion();

	public abstract Property<SourceElement> getTemplate();

	public abstract Property<PluginManagementBlock> getPluginManagementBlock();

	public abstract ConfigurableFileCollection getTemplateSourceSet();

	public abstract ConfigurableFileCollection getGroovyDslSourceSet();

	public abstract ConfigurableFileCollection getKotlinDslSourceSet();

	public abstract ConfigurableFileCollection getContentSourceSet();

	public abstract MapProperty<String, String> getAttributes();

	public abstract ConfigurableFileCollection getAsciinemaSourceSet();

	public abstract ConfigurableFileCollection getStageSourceSet();

	public abstract ConfigurableFileCollection getArchiveSourceSet();
}
