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
package dev.nokee.language.jvm.internal.plugins;

import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Streams;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.CompileTaskComponent;
import dev.nokee.language.jvm.internal.GroovySourceSetSpec;
import dev.nokee.language.jvm.internal.JavaSourceSetSpec;
import dev.nokee.language.jvm.internal.JvmSourceSetTag;
import dev.nokee.language.jvm.internal.KotlinSourceSetSpec;
import dev.nokee.language.jvm.internal.SourceSetComponent;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelTypeUtils;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getPlugins().withType(JavaBasePlugin.class, ignored -> {
			// ComponentFromEntity<FullyQualifiedNameComponent> read-only (on parent only)
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(JvmSourceSetTag.class), ModelComponentReference.of(ParentComponent.class), (entity, projection, parent) -> {
				val sourceSetRegistry = NamedDomainObjectRegistry.of(project.getExtensions().getByType(SourceSetContainer.class));
				val sourceSetProvider = sourceSetRegistry.registerIfAbsent(parent.get().get(FullyQualifiedNameComponent.class).get().toString());
				entity.addComponent(new SourceSetComponent(sourceSetProvider));
			})));

			val registry = project.getExtensions().getByType(ModelRegistry.class);
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(JavaSourceSetSpec.Tag.class), ModelComponentReference.of(IdentifierComponent.class), (entity, tag, identifier) -> {
				val compileTask = registry.register(newEntity(identifier.get().child(TaskName.of("compile")), JavaCompile.class, it -> it.ownedBy(entity)));
				entity.addComponent(new CompileTaskComponent(ModelNodes.of(compileTask)));
			})));
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(GroovySourceSetSpec.Tag.class), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
				val compileTask = registry.register(newEntity(identifier.get().child(TaskName.of("compile")), GroovyCompile.class, it -> it.ownedBy(entity)));
				entity.addComponent(new CompileTaskComponent(ModelNodes.of(compileTask)));
			})));
			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(KotlinSourceSetSpec.Tag.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(SourceSetComponent.class), (entity, projection, identifier, parent, sourceSet) -> {
				final Provider<SourceSet> sourceSetProvider = sourceSet.get();
				@SuppressWarnings("unchecked")
				val KotlinCompile  = (Class<Task>) ModelTypeUtils.toUndecoratedType(sourceSetProvider.map(it -> Streams.stream(project.getTasks().getCollectionSchema().getElements()).filter(t -> t.getName().equals(it.getCompileTaskName("kotlin"))).map(t -> t.getPublicType().getConcreteClass()).collect(MoreCollectors.onlyElement())).get());
				val compileTask = registry.register(newEntity(identifier.get().child(TaskName.of("compile")), KotlinCompile, it -> it.ownedBy(entity)));
				entity.addComponent(new CompileTaskComponent(ModelNodes.of(compileTask)));
			})));
		});
	}
}
