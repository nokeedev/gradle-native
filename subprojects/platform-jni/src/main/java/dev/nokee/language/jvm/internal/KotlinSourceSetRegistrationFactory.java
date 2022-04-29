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
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.internal.ComponentIdentity;
import dev.nokee.platform.base.internal.ComponentName;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Namer;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskDependency;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class KotlinSourceSetRegistrationFactory {
	private final NamedDomainObjectRegistry<SourceSet> sourceSetRegistry;
	private final LanguageSourceSetRegistrationFactory languageSourceSetRegistrationFactory;
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

	public KotlinSourceSetRegistrationFactory(NamedDomainObjectRegistry<SourceSet> sourceSetRegistry, LanguageSourceSetRegistrationFactory languageSourceSetRegistrationFactory) {
		this.sourceSetRegistry = sourceSetRegistry;
		this.languageSourceSetRegistrationFactory = languageSourceSetRegistrationFactory;
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		assert identifier.getName().get().equals("kotlin");
		val sourceSetProvider = sourceSetRegistry.registerIfAbsent(sourceSetNamer.determineName(identifier.getOwnerIdentifier()));
		return languageSourceSetRegistrationFactory.create(identifier, KotlinSourceSet.class, DefaultKotlinSourceSet.class, sourceSetProvider.map(this::asSourceDirectorySet)::get)
			.withComponent(JvmSourceSetTag.tag())
			.build();
	}

	private SourceDirectorySet asSourceDirectorySet(SourceSet sourceSet) {
		try {
			val kotlinSourceSet = new DslObject(sourceSet).getConvention().getPlugins().get("kotlin");
			val DefaultKotlinSourceSet = kotlinSourceSet.getClass();
			val getKotlin = DefaultKotlinSourceSet.getMethod("getKotlin");
			return (SourceDirectorySet) getKotlin.invoke(kotlinSourceSet);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static class DefaultKotlinSourceSet implements KotlinSourceSet, HasPublicType, ModelBackedLanguageSourceSetLegacyMixIn<KotlinSourceSet> {
		public ConfigurableSourceSet getSource() {
			return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get();
		}

		@Override
		public TaskDependency getBuildDependencies() {
			return getSource().getBuildDependencies();
		}

		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(KotlinSourceSet.class);
		}
	}
}
