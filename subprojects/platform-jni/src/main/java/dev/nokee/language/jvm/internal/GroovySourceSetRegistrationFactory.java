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
package dev.nokee.language.jvm.internal;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.jvm.GroovySourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.ComponentIdentity;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Namer;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.GroovyCompile;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.utils.TaskDependencyUtils.of;

public final class GroovySourceSetRegistrationFactory {
	private final NamedDomainObjectRegistry<SourceSet> sourceSetRegistry;
	private final LanguageSourceSetRegistrationFactory languageSourceSetRegistrationFactory;
	private final JvmCompileTaskRegistrationActionFactory compileTaskRegistrationFactory;
	private final Namer<DomainObjectIdentifier> sourceSetNamer = new Namer<DomainObjectIdentifier>() {
		@Override
		public String determineName(DomainObjectIdentifier identifier) {
			return StringUtils.uncapitalize(Streams.stream(identifier).flatMap(it -> {
				if (it instanceof ProjectIdentifier) {
					return Stream.empty();
				} else if (it instanceof ComponentIdentity) {
					return Stream.of(((ComponentIdentity) it).getName()).filter(name -> !(name.isMain() && Iterables.getLast(identifier) != it)).map(ComponentName::toString);
				} else if (it instanceof HasName) {
					return Stream.of(((HasName) it).getName().toString());
				} else {
					throw new UnsupportedOperationException();
				}
			}).map(StringUtils::capitalize).collect(Collectors.joining()));

		}
	};

	public GroovySourceSetRegistrationFactory(NamedDomainObjectRegistry<SourceSet> sourceSetRegistry, LanguageSourceSetRegistrationFactory languageSourceSetRegistrationFactory, JvmCompileTaskRegistrationActionFactory compileTaskRegistrationFactory) {
		this.sourceSetRegistry = sourceSetRegistry;
		this.languageSourceSetRegistrationFactory = languageSourceSetRegistrationFactory;
		this.compileTaskRegistrationFactory = compileTaskRegistrationFactory;
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		assert identifier.getName().get().equals("groovy");
		val sourceSetProvider = sourceSetRegistry.registerIfAbsent(sourceSetNamer.determineName(identifier.getOwnerIdentifier()));
		return languageSourceSetRegistrationFactory.create(identifier, GroovySourceSet.class, DefaultGroovySourceSet.class, sourceSetProvider.map(this::asSourceDirectorySet)::get)
			.action(compileTaskRegistrationFactory.create(identifier, GroovyCompile.class))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(LanguageSourceSetIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, id, ignored) -> {
				if (id.equals(identifier)) {
					sourceSetProvider.configure(task -> ModelStates.realize(entity));
				}
			}))
			.build();
	}

	private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
		return ((org.gradle.api.tasks.GroovySourceSet) new DslObject(sourceSet).getConvention().getPlugins().get("groovy")).getGroovy();
	}

	public static class DefaultGroovySourceSet implements GroovySourceSet, ModelBackedLanguageSourceSetLegacyMixIn<GroovySourceSet> {
		public ConfigurableSourceSet getSource() {
			return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get();
		}

		public TaskProvider<GroovyCompile> getCompileTask() {
			return (TaskProvider<GroovyCompile>) ModelElements.of(this).element("compile", GroovyCompile.class).asProvider();
		}

		@Override
		public TaskDependency getBuildDependencies() {
			return TaskDependencyUtils.composite(getSource().getBuildDependencies(), of(getCompileTask()));
		}
	}
}
