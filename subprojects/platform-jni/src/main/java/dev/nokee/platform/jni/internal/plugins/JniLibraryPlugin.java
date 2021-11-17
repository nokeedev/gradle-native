/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.c.internal.plugins.CLanguagePlugin;
import dev.nokee.language.cpp.internal.plugins.CppLanguagePlugin;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguagePlugin;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguagePlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.internal.IncompatiblePluginUsage;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.runtime.darwin.internal.plugins.DarwinFrameworkResolutionSupportPlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.plugins.NativeBasePlugin;
import org.gradle.process.CommandLineArgumentProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.nokee.platform.jni.internal.plugins.JniLibraryPlugin.IncompatiblePluginsAdvice.*;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.finalizeModelNodeOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

public class JniLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	private static final Set<Class<? extends Plugin<Project>>> NATIVE_LANGUAGE_PLUGINS = ImmutableSet.of(CLanguagePlugin.class, CppLanguagePlugin.class, ObjectiveCLanguagePlugin.class, ObjectiveCppLanguagePlugin.class);

	private final ToolChainSelectorInternal toolChainSelectorInternal;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;

	@Inject
	public JniLibraryPlugin(ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ConfigurationContainer configurations, ProjectLayout layout, DependencyHandler dependencyHandler) {
		this.objects = objects;
		this.providers = providers;
		this.tasks = tasks;
		this.configurations = configurations;
		this.layout = layout;
		this.toolChainSelectorInternal = objects.newInstance(ToolChainSelectorInternal.class);
		this.dependencyHandler = dependencyHandler;
	}

	@Override
	public void apply(Project project) {
		IncompatiblePluginUsage.forProject(project)
			.assertPluginIds(SOFTWARE_MODEL_PLUGIN_IDS, IncompatiblePluginsAdvice::forSoftwareModelNativePlugins)
			.assertPluginId(JAVA_APPLICATION_PLUGIN_ID, IncompatiblePluginsAdvice::forJavaApplicationEntryPointPlugin)
			.assertPluginId(JAVA_LIBRARY_PLUGIN_ID, IncompatiblePluginsAdvice::forJavaLibraryEntryPointPlugin)
			.assertPluginIds(CURRENT_MODEL_PLUGIN_IDS, IncompatiblePluginsAdvice::forCurrentModelNativePlugins)
			.assertPluginClass(NativeBasePlugin.class, IncompatiblePluginsAdvice::forNativeBasePlugin);

		project.getPluginManager().apply("base");
		project.getPluginManager().apply("dev.nokee.jni-library-base");
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		project.getPluginManager().apply(NativeRuntimePlugin.class);

		val extension = registerExtension(project);

		// TODO: On `java` apply, just apply the `java-library` (but don't allow other users to apply it
		project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, extension));
		project.getPlugins().whenPluginAdded(appliedPlugin -> {
			if (isNativeLanguagePlugin(appliedPlugin)) {
				project.getPluginManager().apply(DarwinFrameworkResolutionSupportPlugin.class);
			}
		});

		project.afterEvaluate(proj -> {
			// Ensure the variants are resolved so all tasks are registered.
			getTasks().named("tasks", task -> {
				task.dependsOn((Callable) () -> {
					ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
					return emptyList();
				});
			});
			// Ensure the variants are resolved so all configurations and dependencies are registered.
			getTasks().named("dependencies", task -> {
				task.dependsOn((Callable) () -> {
					ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
					return emptyList();
				});
			});
			getTasks().named("outgoingVariants", task -> {
				task.dependsOn((Callable) () -> {
					ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
					return emptyList();
				});
			});
		});
		// Differ this rules until after the project is evaluated to avoid interfering with other plugins
		project.afterEvaluate(proj -> {
			// The previous trick doesn't work for dependencyInsight task and vice-versa.
			project.getConfigurations().addRule("Java Native Interface (JNI) variants are resolved only when needed.", it -> {
				ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
			});
		});
		project.afterEvaluate(finalizeModelNodeOf(extension));
	}

	private static boolean isNativeLanguagePlugin(Plugin<Project> appliedPlugin) {
		return NATIVE_LANGUAGE_PLUGINS.stream().anyMatch(it -> it.isAssignableFrom(appliedPlugin.getClass()));
	}

	private JavaNativeInterfaceLibrary registerExtension(Project project) {
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val identifier = ComponentIdentifier.builder().name(ComponentName.ofMain()).displayName("JNI library").withProjectIdentifier(ProjectIdentifier.of(project)).build();

		val component = registry.register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class);
		component.configure(it -> it.getBaseName().convention(project.getName()));
		component.configure(it -> {
			it.getVariants().configureEach(variant -> {
				variant.getResourcePath().convention(project.provider(() -> {
					val group = getResourcePath(project.getGroup());
					val dimensions = ((BaseVariant) variant).getIdentifier().getAmbiguousDimensions().getAsKebabCase();
					return group.map(lhs -> dimensions.map(rhs -> lhs + "/" + rhs).orElse(lhs)).orElse(dimensions.orElse(""));
				}));
			});
		});
		val library = component.get();

		project.getExtensions().add(JavaNativeInterfaceLibrary.class, "library", library);
		return library;
	}

	private static Optional<String> getResourcePath(@Nullable Object group) {
		return Optional.ofNullable(group).map(Objects::toString).filter(it -> !it.isEmpty()).map(it -> it.replace('.', '/'));
	}

	private void configureJavaJniRuntime(Project project, JavaNativeInterfaceLibrary library) {
		project.getTasks().named("test", Test.class, task -> {
			Provider<List<FileCollection>> files = library.getVariants().map(JniLibrary::getNativeRuntimeFiles);
			task.dependsOn((Callable<Iterable<File>>)() -> {
				val variant = library.getDevelopmentVariant().getOrNull();
				if (variant == null) {
					return emptyList();
				}
				return variant.getNativeRuntimeFiles();
			});

			// TODO: notify when no native library exists
			task.getJvmArgumentProviders().add(new CommandLineArgumentProvider() {
				@Override
				public Iterable<String> asArguments() {
					String path = files.get().stream().flatMap(it -> it.getFiles().stream()).map(it -> it.getParentFile().getAbsolutePath()).collect(joining(File.pathSeparator));
					return ImmutableList.of("-Djava.library.path=" + path);
				}
			});
		});
	}

	/**
	 * Consider the following guidelines when writing the error message:
	 *  - Clear, short and meaningful
	 *  - No jargon
	 *  - Be humble (no blaming)
	 *  - Provide action
	 *  - Yes answers
	 */
	static abstract class IncompatiblePluginsAdvice {
		static final Set<String> SOFTWARE_MODEL_PLUGIN_IDS = ImmutableSet.of("cpp", "cpp-lang", "c", "c-lang", "objective-c", "objective-c-lang", "objective-cpp", "objective-cpp-lang");
		static final Set<String> CURRENT_MODEL_PLUGIN_IDS = ImmutableSet.of("cpp-library", "cpp-application", "swift-library", "swift-application");
		static final String JAVA_APPLICATION_PLUGIN_ID = "application";
		static final String JAVA_LIBRARY_PLUGIN_ID = "java-library";

		private static final String SOFTWARE_MODEL_MIGRATION = "To learn more about software model migration, visit https://nokee.dev/docs/migrating-from-software-model";
		private static final String CURRENT_MODEL_MIGRATION = "To learn more about Gradle core native plugin migration, visit https://nokee.dev/docs/migrating-from-core-plugins";
		private static final String PROJECT_ENTRY_POINT = "To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points";
		private static final String LEARN_CPP_LANGUAGE = "To learn more about 'dev.nokee.cpp-language' plugin, visit https://nokee.dev/docs/cpp-language-plugin";
		private static final String USE_CPP_LANGUAGE = "Use 'dev.nokee.cpp-language' plugin instead of the 'cpp-application' and 'cpp-library' plugins";
		private static final String VOTE_SWIFT_LANGUAGE = "Vote on https://github.com/nokeedev/gradle-native/issues/26 issue to show interest for Swift language support";
		private static final String REMOTE_SWIFT_PLUGINS = "Remove 'swift-application' and 'swift-library' plugins from the project";

		static void forJavaApplicationEntryPointPlugin(String pluginId, IncompatiblePluginUsage.Context context) {
			context.advice("Refer to https://nokee.dev/docs/building-jni-application for learning how to build JNI application")
				.withFootnote(PROJECT_ENTRY_POINT);
		}

		static void forJavaLibraryEntryPointPlugin(String pluginId, IncompatiblePluginUsage.Context context) {
			context.advice("Use 'java' plugin instead of 'java-library' plugin")
				.withFootnote(PROJECT_ENTRY_POINT);
		}

		static void forSoftwareModelNativePlugins(String pluginId, IncompatiblePluginUsage.Context context) {
			switch (pluginId) {
				case "cpp":
				case "cpp-lang":
					context.advice("Use 'dev.nokee.cpp-language' plugin instead of the 'cpp' and 'cpp-lang' plugins")
						.withFootnote(LEARN_CPP_LANGUAGE)
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
				case "c":
				case "c-lang":
					context.advice("Use 'dev.nokee.c-language' plugin instead of the 'c' and 'c-lang' plugins")
						.withFootnote("To learn more about 'dev.nokee.c-language' plugin, visit https://nokee.dev/docs/c-language-plugin")
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
				case "objective-c":
				case "objective-c-lang":
					context.advice("Use 'dev.nokee.objective-c-language' plugin instead of the 'objective-c' and 'objective-c-lang' plugins")
						.withFootnote("To learn more about 'dev.nokee.objective-c-language' plugin, visit https://nokee.dev/docs/objective-c-language-plugin")
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
				case "objective-cpp":
				case "objective-cpp-lang":
					context.advice("Use 'dev.nokee.objective-cpp-language' plugin instead of the 'objective-cpp' and 'objective-cpp-lang' plugins")
						.withFootnote("To learn more about 'dev.nokee.objective-cpp-language' plugin, visit https://nokee.dev/docs/objective-cpp-language-plugin")
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
			}
		}

		static void forCurrentModelNativePlugins(String pluginId, IncompatiblePluginUsage.Context context) {
			switch (pluginId) {
				case "cpp-application":
				case "cpp-library":
					context.advice(USE_CPP_LANGUAGE)
						.withFootnote(LEARN_CPP_LANGUAGE)
						.withFootnote(PROJECT_ENTRY_POINT)
						.withFootnote(CURRENT_MODEL_MIGRATION);
					break;
				case "swift-application":
				case "swift-library":
					context.advice(REMOTE_SWIFT_PLUGINS)
						.withFootnote(PROJECT_ENTRY_POINT);
					context.advice(VOTE_SWIFT_LANGUAGE);
					break;
			}
		}

		static void forNativeBasePlugin(String pluginId, IncompatiblePluginUsage.Context context) {
			context.advice(USE_CPP_LANGUAGE)
				.withFootnote(LEARN_CPP_LANGUAGE)
				.withFootnote(PROJECT_ENTRY_POINT)
				.withFootnote(CURRENT_MODEL_MIGRATION);
			context.advice(REMOTE_SWIFT_PLUGINS)
				.withFootnote(PROJECT_ENTRY_POINT);
			context.advice(VOTE_SWIFT_LANGUAGE);
		}
	}
}
