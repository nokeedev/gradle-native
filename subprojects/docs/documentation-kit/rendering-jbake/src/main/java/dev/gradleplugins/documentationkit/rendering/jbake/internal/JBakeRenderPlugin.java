package dev.gradleplugins.documentationkit.rendering.jbake.internal;

import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.documentationkit.rendering.jbake.JBakeExtension;
import dev.gradleplugins.documentationkit.rendering.jbake.tasks.GenerateJBakeProperties;
import dev.gradleplugins.documentationkit.rendering.jbake.tasks.GenerateRedirection;
import dev.gradleplugins.documentationkit.rendering.jbake.tasks.RenderJBake;
import dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static dev.nokee.utils.TransformerUtils.transformEach;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

public class JBakeRenderPlugin implements Plugin<Project> {
	public static final String ASSETS_CONFIGURATION_NAME = "assets";
	public static final String TEMPLATES_CONFIGURATION_NAME = "templates";
	public static final String CONTENT_CONFIGURATION_NAME = "content";
	public static final String CONFIGURATION_CONFIGURATION_NAME = "configuration";

	public static final String ASSETS_ELEMENTS_CONFIGURATION_NAME = "assetsElements";
	public static final String TEMPLATES_ELEMENTS_CONFIGURATION_NAME = "templatesElements";
	public static final String CONTENT_ELEMENTS_CONFIGURATION_NAME = "contentElements";
	public static final String CONFIGURATION_ELEMENTS_CONFIGURATION_NAME = "configurationElements";
	public static final String BAKED_ELEMENTS_CONFIGURATION_NAME = "bakedElements";

	static final String JBAKE_ASSETS_USAGE_NAME = "jbake-assets";
	static final String JBAKE_TEMPLATES_USAGE_NAME = "jbake-templates";
	static final String JBAKE_CONTENT_USAGE_NAME = "jbake-content";
	static final String JBAKE_CONFIGURATION_USAGE_NAME = "jbake-properties";
	static final String JBAKE_BAKED_USAGE_NAME = "jbake-baked";

	private final TaskContainer tasks;
	private final ProjectLayout layout;
	private final ConfigurationContainer configurations;
	private final DependencyHandler dependencies;
	private final SoftwareComponentFactory softwareComponentFactory;

	@Inject
	public JBakeRenderPlugin(TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations, DependencyHandler dependencies, SoftwareComponentFactory softwareComponentFactory) {
		this.tasks = tasks;
		this.layout = layout;
		this.configurations = configurations;
		this.dependencies = dependencies;
		this.softwareComponentFactory = softwareComponentFactory;
	}

	@Override
	public void apply(Project project) {
		val configurationRegistry = ProjectConfigurationRegistry.forProject(project);

		val extension = project.getExtensions().create("jbake", JBakeExtension.class);
		extension.getAssets().from("src/jbake/assets");
		extension.getTemplates().from("src/jbake/templates");
		extension.getContent().from("src/jbake/content");
		extension.getConfigurations().putAll(project.provider(() -> loadPropertiesFileIfAvailable(layout.getProjectDirectory().file("src/jbake/jbake.properties"))));

		val redirectionTask = tasks.register("redirections", GenerateRedirection.class, task -> {
			task.getDestinationDirectory().convention(layout.getBuildDirectory().dir("tmp/" + task.getName()));
		});
		extension.getContent().from(redirectionTask.flatMap(it -> it.getDestinationDirectory().dir("content")));
		extension.getTemplates().from(redirectionTask.flatMap(it -> it.getDestinationDirectory().dir("templates")));
		extension.getConfigurations().putAll(redirectionTask.map(it -> {
			val result = new HashMap<String, Object>();
			it.getDestinationDirectory().dir("templates").get().getAsFileTree().visit(details -> {
				result.put("template." + FilenameUtils.removeExtension(details.getName()) + ".file", details.getName());
			});
			return result;
		}));

		val bakePropertiesTask = tasks.register("bakeProperties", GenerateJBakeProperties.class, task -> {
			task.getConfigurations().value(extension.getConfigurations()).disallowChanges();
			task.getOutputFile().value(layout.getBuildDirectory().file("tmp/" + task.getName() + "/jbake.properties"));
		});

		val jbake = configurationRegistry.createIfAbsent("jbake", asDeclarable());
		val content = configurationRegistry.createIfAbsent(CONTENT_CONFIGURATION_NAME,
			asResolvable()
				.andThen(attributes(JBAKE_CONTENT_USAGE_NAME))
				.andThen(forArtifactFormat(DIRECTORY_TYPE))
				.andThen(extendsFrom(jbake)));
		val templates = configurationRegistry.createIfAbsent(TEMPLATES_CONFIGURATION_NAME,
			asResolvable()
				.andThen(attributes(JBAKE_TEMPLATES_USAGE_NAME))
				.andThen(forArtifactFormat(DIRECTORY_TYPE))
				.andThen(extendsFrom(jbake)));
		val assets = configurationRegistry.createIfAbsent(ASSETS_CONFIGURATION_NAME,
			asResolvable()
				.andThen(attributes(JBAKE_ASSETS_USAGE_NAME))
				.andThen(forArtifactFormat(DIRECTORY_TYPE))
				.andThen(extendsFrom(jbake)));
		val configuration = configurationRegistry.createIfAbsent(CONFIGURATION_CONFIGURATION_NAME,
			asResolvable()
				.andThen(attributes(JBAKE_CONFIGURATION_USAGE_NAME))
				.andThen(extendsFrom(jbake)));

		project.getDependencies().registerTransform(UnzipTransform.class,
			unzipArtifact(JBAKE_CONTENT_USAGE_NAME, project.getObjects()));
		project.getDependencies().registerTransform(UnzipTransform.class,
			unzipArtifact(JBAKE_ASSETS_USAGE_NAME, project.getObjects()));
		project.getDependencies().registerTransform(UnzipTransform.class,
			unzipArtifact(JBAKE_TEMPLATES_USAGE_NAME, project.getObjects()));
		project.getDependencies().registerTransform(UnzipTransform.class,
			unzipArtifact(JBAKE_BAKED_USAGE_NAME, project.getObjects()));

		val contentElements = configurationRegistry.create(CONTENT_ELEMENTS_CONFIGURATION_NAME,
			asConsumable()
				.andThen(attributes(JBAKE_CONTENT_USAGE_NAME))
				.andThen(artifactOf(extension.getContent())));
		val templatesElements = configurationRegistry.create(TEMPLATES_ELEMENTS_CONFIGURATION_NAME,
			asConsumable()
				.andThen(attributes(JBAKE_TEMPLATES_USAGE_NAME))
				.andThen(artifactOf(extension.getTemplates())));
		val assetsElements = configurationRegistry.create(ASSETS_ELEMENTS_CONFIGURATION_NAME,
			asConsumable()
				.andThen(attributes(JBAKE_ASSETS_USAGE_NAME))
				.andThen(artifactOf(extension.getAssets())));
		val configurationElements = configurationRegistry.create(CONFIGURATION_ELEMENTS_CONFIGURATION_NAME,
			asConsumable()
				.andThen(attributes(JBAKE_CONFIGURATION_USAGE_NAME))
				.andThen(using(project.getObjects(), artifactIfExists(bakePropertiesTask.flatMap(GenerateJBakeProperties::getOutputFile)))));
		val bakedElements = configurationRegistry.create(BAKED_ELEMENTS_CONFIGURATION_NAME,
			asConsumable()
				.andThen(attributes(JBAKE_BAKED_USAGE_NAME))
				.andThen(artifactOf(extension.getDestinationDirectory())));

		val stageTask = project.getTasks().register("stageBake", Sync.class, task -> {
			task.into("content", spec -> spec.from(content).from(extension.getContent()));
			task.into("assets", spec -> spec.from(assets).from(extension.getAssets()));
			task.into("templates", spec -> spec.from(templates).from(extension.getTemplates()));
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
			task.setIncludeEmptyDirs(false);
		});

		val bakeTask = project.getTasks().register("bake", RenderJBake.class, task -> {
			task.setGroup("documentation");
			task.setDescription("Bakes with JBake");
			task.getSourceDirectory().fileProvider(stageTask.map(Sync::getDestinationDir));
			task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("jbake"));
			task.getConfigurations().putAll(configuration.getIncoming().getFiles().getElements().map(
				transformEach(JBakeRenderPlugin::loadPropertiesFileIfAvailable)
					.andThen(JBakeRenderPlugin::mergeConfigurations)));
			task.getConfigurations().put("working.directory", stageTask.map(this::relativeToProjectDirectory));
			task.getConfigurations().putAll(extension.getConfigurations());
			task.getClasspath()
				.from(jbake("2.6.5"))
				.from(asciidoctor("2.2.0"))
				.from(groovyTemplates("3.0.2"))
				.from(flexmarkTemplates("0.61.0"))
				.from(freemarkerTemplates("2.3.30"))
				.from(pegdownTemplates("1.6.0"))
				.from(thymeleafTemplates("3.0.11.RELEASE"))
				.from(jade4jTemplates("1.2.7"))
				.from(pebbleTemplates("3.1.4"))
				;
		});
		extension.getDestinationDirectory().value(bakeTask.flatMap(RenderJBake::getDestinationDirectory)).disallowChanges();

		val jbakeComponent = softwareComponentFactory.adhoc("jbake");
		jbakeComponent.addVariantsFromConfiguration(contentElements, skipIf(hasUnpublishableArtifactType()));
		jbakeComponent.addVariantsFromConfiguration(templatesElements, skipIf(hasUnpublishableArtifactType()));
		jbakeComponent.addVariantsFromConfiguration(assetsElements, skipIf(hasUnpublishableArtifactType()));
		jbakeComponent.addVariantsFromConfiguration(configurationElements, skipIf(hasUnpublishableArtifactType()));
		jbakeComponent.addVariantsFromConfiguration(bakedElements, skipIf(hasUnpublishableArtifactType()));
		project.getComponents().add(jbakeComponent);
	}

	private String relativeToProjectDirectory(Sync task) {
		return layout.getProjectDirectory().getAsFile().toPath().relativize(task.getDestinationDir().toPath()).toString();
	}

	private FileCollection jbake(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.jbake:jbake-core:" + version));
	}

	// TODO: Move to asciidoctor-language plugin
	private FileCollection asciidoctor(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.asciidoctor:asciidoctorj:" + version));
	}

	// TODO: Move to groovy-templates plugin?
	private FileCollection groovyTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.codehaus.groovy:groovy:" + version), dependencies.create("org.codehaus.groovy:groovy-templates:" + version));
	}

	// TODO: Move to flexmark-templates plugin?
	private FileCollection flexmarkTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("com.vladsch.flexmark:flexmark:" + version), dependencies.create("com.vladsch.flexmark:flexmark-profile-pegdown:" + version));
	}

	// TODO: Move to freemarker-templates plugin?
	private FileCollection freemarkerTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.freemarker:freemarker:" + version));
	}

	// TODO: Move to pegdown-templates plugin?
	private FileCollection pegdownTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.pegdown:pegdown:" + version));
	}

	// TODO: Move to thymeleaf-templates plugin?
	private FileCollection thymeleafTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("org.thymeleaf:thymeleaf:" + version));
	}

	// TODO: Move to jade4j-templates plugin?
	private FileCollection jade4jTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("de.neuland-bfi:jade4j:" + version));
	}

	// TODO: Move to pebble-templates plugin?
	private FileCollection pebbleTemplates(String version) {
		return configurations.detachedConfiguration(dependencies.create("io.pebbletemplates:pebble:" + version));
	}

	private static Map<String, Object> loadPropertiesFileIfAvailable(FileSystemLocation propertiesFile) {
		if (!propertiesFile.getAsFile().exists()) {
			return ImmutableMap.of();
		}

		val properties = new Properties();
		try (val inStream = new FileInputStream(propertiesFile.getAsFile())) {
			properties.load(inStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		val builder = ImmutableMap.<String, Object>builder();
		properties.forEach((key, value) -> builder.put(key.toString(), value));
		return builder.build();
	}

	private static Map<String, Object> mergeConfigurations(Iterable<Map<String, Object>> configurations) {
		val builder = ImmutableMap.<String, Object>builder();
		configurations.forEach(builder::putAll);
		return builder.build();
	}

	private static ActionUtils.Action<Configuration> attributes(String usage) {
		return forUsage(usage).andThen(forDocsType(usage));
	}

	private static final Attribute<String> ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);
	private static Action<TransformSpec<TransformParameters.None>> unzipArtifact(String targetUsage, ObjectFactory objects) {
		return variantTransform -> {
			variantTransform.getFrom().attribute(ARTIFACT_FORMAT, ZIP_TYPE);
			variantTransform.getFrom().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, targetUsage));
			variantTransform.getTo().attribute(ARTIFACT_FORMAT, DIRECTORY_TYPE);
			variantTransform.getTo().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, targetUsage));
		};
	}
}
