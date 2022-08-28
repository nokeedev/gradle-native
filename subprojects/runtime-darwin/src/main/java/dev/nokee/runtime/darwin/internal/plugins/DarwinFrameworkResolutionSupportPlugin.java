/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.runtime.darwin.internal.plugins;

import dev.nokee.gradle.AdhocArtifactRepository;
import dev.nokee.gradle.AdhocArtifactRepositoryFactory;
import dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin;
import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.darwin.internal.FrameworkHandler;
import dev.nokee.runtime.darwin.internal.locators.XcodebuildLocator;
import dev.nokee.runtime.darwin.internal.locators.XcrunLocator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
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

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Capability.ofCapability;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Component.ofComponent;
import static dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin.NOKEE_SERVER_SERVICE_NAME;
import static dev.nokee.runtime.darwin.internal.DarwinArtifactTypes.FRAMEWORK_TYPE;
import static dev.nokee.runtime.darwin.internal.DarwinLibraryElements.FRAMEWORK_BUNDLE;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.DESERIALIZED;
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.SERIALIZED;
import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;

public class DarwinFrameworkResolutionSupportPlugin implements Plugin<Project> {
	private static final Logger LOGGER = Logging.getLogger(DarwinFrameworkResolutionSupportPlugin.class);
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public DarwinFrameworkResolutionSupportPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(DarwinRuntimePlugin.class);
		project.getPluginManager().apply(CompatibilityRules.class);

		if (SystemUtils.IS_OS_MAC) {
			project.getRepositories().add(createFrameworkRepository(project));
			project.getPluginManager().apply(FakeMavenRepositoryPlugin.class);

			NokeeServerService.Parameters parameters = (NokeeServerService.Parameters)project.getGradle().getSharedServices().getRegistrations().getByName(NOKEE_SERVER_SERVICE_NAME).getParameters();
			parameters.getToolLocators().add(XcrunLocator.class.getCanonicalName());
			parameters.getToolLocators().add(XcodebuildLocator.class.getCanonicalName());

			configure(project.getDependencies());
		} else {
			LOGGER.debug("Darwin framework resolution support is not configured because detected operating system is not macOS.");
		}
	}

	private static AdhocArtifactRepository createFrameworkRepository(Project project) {
		val repository = AdhocArtifactRepositoryFactory.forProject(project).create();
		repository.setName("framework");
		repository.getCacheDirectory().set(project.getLayout().getBuildDirectory().dir("m2/framework"));
		repository.content(content -> content.includeGroup("dev.nokee.framework"));

		val toolRepository = new ToolRepository();
		toolRepository.register("xcrun", new XcrunLocator());
		toolRepository.register("xcodebuild", new XcodebuildLocator());

		val handler = new FrameworkHandler(toolRepository);
		repository.setComponentSupplier(handler);
		repository.setComponentVersionLister(handler);
		return repository;
	}

	public void configure(DependencyHandler dependencies) {
		dependencies.artifactTypes(it -> {
			it.create("localpath", type -> type.getAttributes().attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED));
		});
		dependencies.registerTransform(DeserializeLocalFramework.class, variantTransform -> {
			variantTransform.getFrom()
				.attribute(ARTIFACT_TYPE_ATTRIBUTE, "localpath")
				.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, FRAMEWORK_BUNDLE))
				.attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, SERIALIZED);
			variantTransform.getTo()
				.attribute(ARTIFACT_TYPE_ATTRIBUTE, FRAMEWORK_TYPE)
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
