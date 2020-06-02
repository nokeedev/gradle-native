package dev.nokee.runtime.darwin.internal.plugins;

import dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin;
import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.darwin.internal.FrameworkRouteHandler;
import dev.nokee.runtime.darwin.internal.locators.XcodebuildLocator;
import dev.nokee.runtime.darwin.internal.locators.XcrunLocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin.NOKEE_SERVER_SERVICE_NAME;
import static dev.nokee.runtime.base.internal.repositories.NokeeServerService.NOKEE_LOCAL_REPOSITORY_NAME;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.*;
import static dev.nokee.runtime.nativebase.internal.ArtifactTypes.ARTIFACT_TYPES_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactTypes.FRAMEWORK_TYPE;
import static dev.nokee.runtime.nativebase.internal.LibraryElements.FRAMEWORK_BUNDLE;

public abstract class DarwinFrameworkResolutionSupportPlugin implements Plugin<Project> {
	private static final Logger LOGGER = Logging.getLogger(DarwinFrameworkResolutionSupportPlugin.class);

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CompatibilityRules.class);

		if (SystemUtils.IS_OS_MAC) {
			project.getPluginManager().apply(FakeMavenRepositoryPlugin.class);

			NokeeServerService.Parameters parameters = (NokeeServerService.Parameters)project.getGradle().getSharedServices().getRegistrations().getByName(NOKEE_SERVER_SERVICE_NAME).getParameters();
			parameters.getRouteHandlers().add(FrameworkRouteHandler.class.getCanonicalName());
			parameters.getToolLocators().add(XcrunLocator.class.getCanonicalName());
			parameters.getToolLocators().add(XcodebuildLocator.class.getCanonicalName());

			configure(project.getDependencies());

			project.getRepositories().withType(MavenArtifactRepository.class).configureEach(repo -> {
				if (NOKEE_LOCAL_REPOSITORY_NAME.equals(repo.getName())) {
					repo.mavenContent(content -> {
						content.includeGroup("dev.nokee.framework");
					});
				}
			});
		} else {
			LOGGER.debug("Darwin framework resolution support is not configured because detected operating system is not macOS.");
		}
	}

	public void configure(DependencyHandler dependencies) {
		dependencies.artifactTypes(it -> {
			it.create("localpath", type -> type.getAttributes().attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED));
			it.create(FRAMEWORK_TYPE);
		});
		dependencies.registerTransform(DeserializeLocalFramework.class, variantTransform -> {
			variantTransform.getFrom()
				.attribute(ARTIFACT_TYPES_ATTRIBUTE, "localpath")
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED);
			variantTransform.getTo()
				.attribute(ARTIFACT_TYPES_ATTRIBUTE, FRAMEWORK_TYPE)
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, DESERIALIZED);
		});
	}

	public static abstract class DeserializeLocalFramework implements TransformAction<TransformParameters.None> {
		@InputArtifact
		public abstract Provider<FileSystemLocation> getInputArtifact();

		public void transform(TransformOutputs outputs) {
			try {
				String s = FileUtils.readFileToString(getInputArtifact().get().getAsFile(), Charset.defaultCharset());
				File framework = new File(s);
				File o = outputs.dir(framework.getName());
				if (!o.delete()) {
					throw new RuntimeException("Can't delete file");
				}
				Files.createSymbolicLink(o.toPath(), framework.toPath());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
