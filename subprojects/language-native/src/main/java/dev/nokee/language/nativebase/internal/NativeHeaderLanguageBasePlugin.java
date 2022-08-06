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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.utils.Optionals.stream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;

public class NativeHeaderLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new HasConfigurableHeadersMixInRule(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(SourceSetFactory.class), project.getObjects())));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new HeaderSearchPathsConfigurationRegistrationAction(project.getExtensions().getByType(ModelRegistry.class), project.getObjects())));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachHeaderSearchPathsToCompileTaskRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new NativeCompileTaskDefaultConfigurationRule(project.getExtensions().getByType(ModelRegistry.class)));

		project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelAction.configureEach(NativeHeaderSet.class, sourceSet -> {
			if (ModelNodes.of(sourceSet).find(ElementNameComponent.class).map(it -> it.get().equals(ElementName.of("public"))).orElse(false)) {
				sourceSet.from((Callable<?>) () -> {
					return ModelNodes.of(sourceSet).find(ParentComponent.class).flatMap(parent -> {
						ModelStates.finalize(parent.get());
						return ParentUtils.stream(parent).flatMap(it -> stream(it.find(PublicHeadersComponent.class))).findFirst()
							.map(it -> (Object) it.get());
					}).orElse(Collections.emptyList());
				});
			} else if (ModelNodes.of(sourceSet).find(ElementNameComponent.class).map(it -> it.get().equals(ElementName.of("headers"))).orElse(false)) {
				sourceSet.from((Callable<?>) () -> {
					return ModelNodes.of(sourceSet).find(ParentComponent.class).flatMap(parent -> {
						ModelStates.finalize(parent.get());
						return ParentUtils.stream(parent).flatMap(it -> stream(it.find(PrivateHeadersComponent.class))).findFirst()
							.map(it -> (Object) it.get());
					}).orElse(Collections.emptyList());
				});
			}
		}));

		// ComponentFromEntity<GradlePropertyComponent> read-write on PrivateHeadersPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(PrivateHeadersPropertyComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, cSources, fullyQualifiedName) -> {
			((ConfigurableFileCollection) cSources.get().get(GradlePropertyComponent.class).get()).from("src/" + fullyQualifiedName.get() + "/headers");
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(HasPrivateHeadersMixIn.Tag.class), (entity, ignored) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val property = ModelStates.register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("privateHeaders"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(ModelPropertyRegistrationFactory.fileCollectionProperty())
				.build()));
			entity.addComponent(new PrivateHeadersPropertyComponent(property));
		})));
		// ComponentFromEntity<GradlePropertyComponent> read-write on HasConfigurableHeadersPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeHeaderSetTag.class), ModelComponentReference.of(HasConfigurableHeadersPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored1, headers, parent) -> {
			((ConfigurableFileCollection) headers.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				ModelStates.finalize(parent.get());
				return ParentUtils.stream(parent).flatMap(it -> stream(it.find(PrivateHeadersComponent.class))).findFirst()
					.map(it -> (Object) it.get()).orElse(Collections.emptyList());
			});
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on PrivateHeadersPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(PrivateHeadersPropertyComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, privateHeaders, ignored1) -> {
			ModelStates.finalize(privateHeaders.get());
			val sources = (ConfigurableFileCollection) privateHeaders.get().get(GradlePropertyComponent.class).get();
			// Note: We should be able to use finalizeValueOnRead but Gradle discard task dependencies
			entity.addComponent(new PrivateHeadersComponent(/*finalizeValueOnRead*/(disallowChanges(sources))));
		}));

		// ComponentFromEntity<GradlePropertyComponent> read-write on PublicHeadersPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(PublicHeadersPropertyComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, cSources, fullyQualifiedName) -> {
			((ConfigurableFileCollection) cSources.get().get(GradlePropertyComponent.class).get()).from("src/" + fullyQualifiedName.get() + "/public");
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(HasPublicHeadersMixIn.Tag.class), (entity, ignored) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val property = ModelStates.register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("publicHeaders"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(ModelPropertyRegistrationFactory.fileCollectionProperty())
				.build()));
			entity.addComponent(new PublicHeadersPropertyComponent(property));
		})));
		// ComponentFromEntity<GradlePropertyComponent> read-write on HasConfigurableHeadersPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeHeaderSetTag.class), ModelComponentReference.of(HasConfigurableHeadersPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored1, headers, parent) -> {
			((ConfigurableFileCollection) headers.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				ModelStates.finalize(parent.get());
				return ParentUtils.stream(parent).flatMap(it -> stream(it.find(PublicHeadersComponent.class))).findFirst()
					.map(it -> (Object) it.get()).orElse(Collections.emptyList());
			});
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on PublicHeadersPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(PublicHeadersPropertyComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, publicHeaders, ignored1) -> {
			ModelStates.finalize(publicHeaders.get());
			val sources = (ConfigurableFileCollection) publicHeaders.get().get(GradlePropertyComponent.class).get();
			// Note: We should be able to use finalizeValueOnRead but Gradle discard task dependencies
			entity.addComponent(new PublicHeadersComponent(/*finalizeValueOnRead*/(disallowChanges(sources))));
		}));
	}
}
