/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.language.swift.internal.plugins;

import com.google.common.base.Preconditions;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.nativebase.internal.ConfigurationUtilsEx;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.language.nativebase.internal.plugins.LanguageNativeBasePlugin;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.internal.SwiftSourceSetSpec;
import dev.nokee.language.swift.internal.rules.AttachImportModulesToCompileTaskRule;
import dev.nokee.language.swift.internal.rules.ImportModulesConfigurationRegistrationAction;
import dev.nokee.language.swift.internal.rules.SwiftCompileTaskDefaultConfigurationRule;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.KnownModelObject;
import dev.nokee.model.internal.KnownModelObjectTypeOf;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ElementExportingSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.scripts.DefaultImporter;
import dev.nokee.utils.ConfigurationUtils;
import dev.nokee.utils.TextCaseUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.tasks.Sync;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import java.util.Iterator;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static dev.nokee.utils.TransformerUtils.to;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(SwiftSourceSetSpec.class);

		DefaultImporter.forProject(project).defaultImport(SwiftSourceSet.class);

		sources(project).withType(SwiftSourceSetSpec.class).configureEach(sourceSet -> {
			sourceSet.getImportModules().extendsFrom(sourceSet.getDependencies().getCompileOnly());
		});

		sources(project).withType(SwiftSourceSetSpec.class).configureEach(new ImportModulesConfigurationRegistrationAction(project.getObjects()));
		sources(project).withType(SwiftSourceSetSpec.class).configureEach(new AttachImportModulesToCompileTaskRule());
		sources(project).withType(SwiftSourceSetSpec.class).configureEach(new SwiftCompileTaskDefaultConfigurationRule(project.getProviders()));

		project.getTasks().withType(SwiftCompileTask.class).configureEach(withElement((element, task) -> {
			task.getModuleName().set(project.provider(() -> {
				return element.getParents()
					.filter(it -> it.instanceOf(HasBaseName.class))
					.map(it -> it.safeAs(HasBaseName.class).flatMap(HasBaseName::getBaseName))
					.findFirst()
					.orElseGet(() -> project.provider(() -> null));
			}).flatMap(it -> it).map(TextCaseUtils::toCamelCase));
		}));

		sources(project).withType(SwiftSourceSetSpec.class).configureEach(sourceSet -> {
			sourceSet.getParents().filter(it -> it.getIdentifier() instanceof VariantIdentifier).findFirst().map(it -> (VariantIdentifier) it.getIdentifier()).ifPresent(variantIdentifier -> {
				final Configuration importModules = sourceSet.getImportModules().getAsConfiguration();
				ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) variantIdentifier.getBuildVariant(), project.getObjects()).execute(importModules);
				ConfigurationUtilsEx.configureAsGradleDebugCompatible(importModules);
			});
		});

		model(project, objects()).whenElementKnown(ofType(new KnownModelObjectTypeOf<>(NativeSourcesAware.class), new Action<KnownModelObject<NativeSourcesAware>>() {
			@Override
			public void execute(KnownModelObject<NativeSourcesAware> component) {
				if (component.getType().isSubtypeOf(ElementExportingSpec.class)) {
					final String name = ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child(ElementName.of("importModuleElements"))).toString();

					final Configuration apiElements = project.getConfigurations().maybeCreate(name);
					apiElements.setCanBeResolved(false);
					apiElements.setCanBeConsumed(true);

					// FIXME: Not perfect but good enough for now
					//    Spoiler: it doesn't account if the declarable bucket has prefix (like in JNI library)
					//    Note: JNI library doesn't export any native elements
					project.getConfigurations().matching(it -> {
						return it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child(ElementName.of("api"))).toString()) || it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child(ElementName.of("compileOnlyApi"))).toString());
					}).all(apiElements::extendsFrom);


					ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))).execute(apiElements);
					if (component.getIdentifier() instanceof VariantIdentifier) {
						final VariantIdentifier variantIdentifier = (VariantIdentifier) component.getIdentifier();
						ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variantIdentifier.getBuildVariant(), project.getObjects()).execute(apiElements);
					}
					ConfigurationUtilsEx.configureAsGradleDebugCompatible(apiElements);

					val syncTask = model(project, registryOf(Task.class)).register(component.getIdentifier().child(TaskName.of("sync", "importModule")), Sync.class);
					syncTask.configure(task -> {
						val exportedSwiftModule = project.provider(() -> {
							return component.asProvider().map(to(BinaryAwareComponent.class)).map(it -> it.getBinaries().withType(NativeBinary.class).map(binary -> {
								return binary.getCompileTasks().withType(SwiftCompileTask.class).map(SwiftCompile::getModuleFile);
							}).flatMap(toProviderOfIterable(project.getObjects()::listProperty)).map(this::one));
						}).flatMap(it -> it);
						task.from(exportedSwiftModule, spec -> spec.rename(it -> TextCaseUtils.toCamelCase(component.asProvider().map(to(HasBaseName.class)).flatMap(HasBaseName::getBaseName).get()) + ".swiftmodule"));
						task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
					});
					apiElements.getOutgoing().artifact(syncTask.asProvider().map(Sync::getDestinationDir));
				}
			}

			private <T> T one(Iterable<T> c) {
				Iterator<T> iterator = c.iterator();
				Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
				T result = iterator.next();
				Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
				return result;
			}
		}));
	}
}
