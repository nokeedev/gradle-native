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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public final class LanguageSourceSetRegistrationFactory {
	private final ObjectFactory objectFactory;
	private final SourceSetFactory sourceSetFactory;
	private final SourcePropertyRegistrationActionFactory sourcePropertyRegistrationFactory;

	public LanguageSourceSetRegistrationFactory(ObjectFactory objectFactory, SourceSetFactory sourceSetFactory, SourcePropertyRegistrationActionFactory sourcePropertyRegistrationFactory) {
		this.objectFactory = objectFactory;
		this.sourceSetFactory = sourceSetFactory;
		this.sourcePropertyRegistrationFactory = sourcePropertyRegistrationFactory;
	}

	public <T extends LanguageSourceSet> ModelRegistration.Builder create(LanguageSourceSetIdentifier identifier, Class<T> publicType) {
		return create(identifier, sourcePropertyRegistrationFactory.create(identifier, sourceSetFactory::sourceSet), createdUsing(ModelType.of(publicType), () -> objectFactory.newInstance(publicType)));
	}

	public <T extends LanguageSourceSet> ModelRegistration.Builder create(LanguageSourceSetIdentifier identifier, Class<? super T> publicType, Class<T> implementationType) {
		return create(identifier, sourcePropertyRegistrationFactory.create(identifier, sourceSetFactory::sourceSet), createdUsing(ModelType.of(publicType), () -> objectFactory.newInstance(implementationType)));
	}

	public <T extends LanguageSourceSet> ModelRegistration.Builder create(LanguageSourceSetIdentifier identifier, Class<T> publicType, SourceDirectorySet sourceSet) {
		return create(identifier, sourcePropertyRegistrationFactory.create(identifier, () -> sourceSetFactory.bridgedSourceSet(sourceSet)), createdUsing(ModelType.of(publicType), () -> objectFactory.newInstance(publicType)));
	}

	public <T extends LanguageSourceSet> ModelRegistration.Builder create(LanguageSourceSetIdentifier identifier, Class<? super T> publicType, Class<T> implementationType, SourceDirectorySet sourceSet) {
		return create(identifier, sourcePropertyRegistrationFactory.create(identifier, () -> sourceSetFactory.bridgedSourceSet(sourceSet)), createdUsing(ModelType.of(publicType), () -> objectFactory.newInstance(implementationType)));
	}

	private ModelRegistration.Builder create(LanguageSourceSetIdentifier identifier, ModelAction sourcePropertyAction, ModelProjection projection) {
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(toPath(identifier))
			.withComponent(IsLanguageSourceSet.tag())
			.withComponent(projection)
			.withComponent(managed(of(DefaultLanguageSourceSetStrategy.class)))
			.action(sourcePropertyAction);
	}
}
