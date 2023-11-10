/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.linking;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjects;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.HasLinkTask;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;

import static dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts.frameworks;
import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;

final class LinkLibrariesConfigurationRegistrationRule implements Action<Artifact> {
	private final ObjectFactory objects;
	private final ModelObjects objs;

	public LinkLibrariesConfigurationRegistrationRule(ObjectFactory objects, ModelObjects objs) {
		this.objects = objects;
		this.objs = objs;
	}

	@Override
	public void execute(Artifact target) {
		if (target instanceof HasLinkLibrariesDependencyBucket) {
			final Configuration linkLibraries = ((HasLinkLibrariesDependencyBucket) target).getLinkLibraries().getAsConfiguration();
			forNativeLinkUsage().execute(linkLibraries);
			if (target instanceof HasLinkTask) {
				val incomingArtifacts = FrameworkAwareIncomingArtifacts.from(incomingArtifactsOf(linkLibraries));

				val incomingLibraries = incomingArtifacts.getAs(frameworks().negate());
				((HasLinkTask<?>) target).getLinkTask().configure(configureLibraries(from(incomingLibraries)));

				val incomingFrameworks = incomingArtifacts.getAs(frameworks());
				((HasLinkTask<?>) target).getLinkTask().configure(configureLinkerArgs(addAll(asFrameworkFlags(incomingFrameworks))));
			}

			// TODO: Try to replace LinkLibrariesExtendsFromParentDependencyBucketAction when binaries are created by variant
//			ModelElementSupport.safeAsModelElement(target).map(ModelElement::getIdentifier).ifPresent(identifier -> {
//				objs.parentsOf(identifier).filter(it -> {
//					return it.instanceOf(DependencyAwareComponent.class) && it.asModelObject(DependencyAwareComponent.class).get().getDependencies() instanceof NativeComponentDependencies;
//				}).map(it -> (NativeComponentDependencies) it.asModelObject(DependencyAwareComponent.class).get()).findFirst().ifPresent(dependencies -> {
//					((HasLinkLibrariesDependencyBucket) target).getLinkLibraries().extendsFrom(dependencies.getImplementation(), dependencies.getLinkOnly());
//				});
//			});
		}
	}

	private Action<Configuration> forNativeLinkUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.NATIVE_LINK)));
	}

	private static Provider<ResolvableDependencies> incomingArtifactsOf(Configuration element) {
		return ProviderUtils.fixed(element.getIncoming());
	}

	//region Link libraries
	public static <SELF extends Task> ActionUtils.Action<SELF> configureLibraries(BiConsumer<? super SELF, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(librariesProperty(task)));
	}

	private static ConfigurableFileCollection librariesProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getLibs();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Compiler arguments
	private static Action<ObjectLink> configureLinkerArgs(BiConsumer<? super ObjectLink, ? super PropertyUtils.CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getLinkerArgs()));
	}

	private static Transformer<Iterable<String>, Path> toFrameworkSearchPathFlags() {
		return it -> ImmutableList.of("-F", it.getParent().toString());
	}

	private static Transformer<Iterable<String>, Path> toFrameworkFlags() {
		return it -> ImmutableList.of("-framework", FilenameUtils.removeExtension(it.getFileName().toString()));
	}

	private static Provider<Iterable<String>> asFrameworkFlags(Provider<Set<Path>> frameworksSearchPaths) {
		return frameworksSearchPaths.map(flatTransformEach(it -> {
			return ImmutableList.<String>builder()
				.addAll(toFrameworkSearchPathFlags().transform(it))
				.addAll(toFrameworkFlags().transform(it))
				.build();
		}));
	}
	//endregion
}
