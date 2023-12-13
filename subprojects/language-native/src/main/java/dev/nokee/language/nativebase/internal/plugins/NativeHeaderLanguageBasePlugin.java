/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.language.nativebase.internal.plugins;

import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.LanguageSourcePropertySpec;
import dev.nokee.language.base.internal.PropertySpec;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.NativeSourceSetComponentDependencies;
import dev.nokee.language.nativebase.internal.BaseNativeSourceSetSpec;
import dev.nokee.language.nativebase.internal.ConfigurationUtilsEx;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.NativeHeaderProperty;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.language.nativebase.internal.PublicHeadersMixIn;
import dev.nokee.language.nativebase.internal.rules.AttachHeaderSearchPathsToCompileTaskRule;
import dev.nokee.language.nativebase.internal.rules.HeaderSearchPathsConfigurationRegistrationAction;
import dev.nokee.language.nativebase.internal.rules.NativeCompileTaskDefaultConfigurationRule;
import dev.nokee.model.internal.KnownModelObjectTypeOf;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ElementExportingSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.runtime.nativebase.internal.NativeArtifactTypes;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.Sync;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;

// TODO: formalize headers visibility with export elements
public class NativeHeaderLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		model(project, objects()).configureEach(ofType(PublicHeadersMixIn.class, it -> {
			val publicHeaders = model(project, registryOf(PropertySpec.class)).register(it.getIdentifier().child("publicHeaders"), NativeHeaderProperty.class);
			publicHeaders.configure(a -> a.getVisibility().set(NativeHeaderProperty.BasicVisibility.Public));
			it.getSourceProperties().add(publicHeaders.get());
		}));

		sources(project).configureEach(new HeaderSearchPathsConfigurationRegistrationAction<>(project.getObjects()));
		sources(project).configureEach(new AttachHeaderSearchPathsToCompileTaskRule<>());
		sources(project).configureEach(new NativeCompileTaskDefaultConfigurationRule<>());
		sources(project).configureEach(sourceSet -> {
			if (sourceSet instanceof HasHeaderSearchPaths && sourceSet instanceof DependencyAwareComponent && ((DependencyAwareComponent<?>) sourceSet).getDependencies() instanceof NativeSourceSetComponentDependencies) {
				((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().extendsFrom(((NativeSourceSetComponentDependencies) ((DependencyAwareComponent<?>) sourceSet).getDependencies()).getCompileOnly());
			}
		});

		sources(project).configureEach(ofType(HasHeaders.class, withElement((element, sourceSet) -> {
			sourceSet.getHeaders().from((Callable<Object>) () -> {
				return element.getParents().flatMap(it -> {
					return it.safeAs(LanguagePropertiesAware.class).map(a -> {
						final String name = ModelObjectIdentifiers.asFullyQualifiedName(a.getIdentifier().child("publicHeaders")).toString();
						return a.getSourceProperties().withType(LanguageSourcePropertySpec.class).findByName(name);
					}).map(Stream::of).getOrElse(Stream.empty());
				}).findFirst().map(a -> (Iterable<?>) a.getSource()).orElse(Collections.emptyList());
			});
		})));
		sources(project).configureEach(ofType(HasHeaders.class, withElement((element, sourceSet) -> {
			sourceSet.getHeaders().from((Callable<Object>) () -> {
				return element.getParents().flatMap(it -> {
					return it.safeAs(LanguagePropertiesAware.class).map(a -> {
						final String name = ModelObjectIdentifiers.asFullyQualifiedName(a.getIdentifier().child("privateHeaders")).toString();
						return a.getSourceProperties().withType(LanguageSourcePropertySpec.class).findByName(name);
					}).map(Stream::of).getOrElse(Stream.empty());
				}).findFirst().map(a -> (Iterable<?>) a.getSource()).orElse(Collections.emptyList());
			});
		})));

		model(project, factoryRegistryOf(PropertySpec.class)).registerFactory(NativeHeaderProperty.class);

		sources(project).withType(BaseNativeSourceSetSpec.class).configureEach(sourceSet -> {
			if (sourceSet instanceof HasHeaderSearchPaths) {
				sourceSet.getParents().filter(it -> it.getIdentifier() instanceof VariantIdentifier).findFirst().map(it -> (VariantIdentifier) it.getIdentifier()).ifPresent(variantIdentifier -> {
					final Configuration headerSearchPaths = ((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().getAsConfiguration();
					ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) variantIdentifier.getBuildVariant(), project.getObjects()).execute(headerSearchPaths);
					ConfigurationUtilsEx.configureAsGradleDebugCompatible(headerSearchPaths);
				});
			}
		});

		model(project, objects()).whenElementKnown(ofType(new KnownModelObjectTypeOf<>(NativeSourcesAware.class), component -> {
			if (component.getType().isSubtypeOf(ElementExportingSpec.class)) {
				final String name = ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child(ElementName.of("headersElements"))).toString();

				final Configuration apiElements = project.getConfigurations().maybeCreate(name);
				apiElements.setCanBeResolved(false);
				apiElements.setCanBeConsumed(true);

				// FIXME: Not perfect but good enough for now
				//    Spoiler: it doesn't account if the declarable bucket has prefix (like in JNI library)
				//    Note: JNI library doesn't export any native elements
				project.getConfigurations().matching(it -> {
					return it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child(ElementName.of("api"))).toString()) || it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child(ElementName.of("compileOnlyApi"))).toString());
				}).all(apiElements::extendsFrom);

				ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))).execute(apiElements);
				if (component.getIdentifier() instanceof VariantIdentifier) {
					final VariantIdentifier variantIdentifier = (VariantIdentifier) component.getIdentifier();
					ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variantIdentifier.getBuildVariant(), project.getObjects()).execute(apiElements);
				}
				ConfigurationUtilsEx.configureAsGradleDebugCompatible(apiElements);

				val syncTask = model(project, registryOf(Task.class)).register(component.getIdentifier().child(TaskName.of("sync", "publicHeaders")), Sync.class);
				syncTask.configure(withElement((element, task) -> {
					task.from((Callable<?>) () -> {
						return element.getParents()
							.map(it -> it.safeAs(ExtensionAware.class).get())
							.flatMap(it -> {
								final FileCollection publicHeaders = (FileCollection) it.getExtensions().findByName("publicHeaders");
								if (publicHeaders == null) {
									return Stream.empty();
								} else {
									return Stream.of(publicHeaders);
								}
							})
							.findFirst().map(Object.class::cast).orElseGet(Collections::emptyList);
					});
					task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
				}));
				apiElements.getOutgoing().artifact(syncTask.asProvider().map(Sync::getDestinationDir),
					it -> it.setType(NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY));
			}
		}));
	}
}
