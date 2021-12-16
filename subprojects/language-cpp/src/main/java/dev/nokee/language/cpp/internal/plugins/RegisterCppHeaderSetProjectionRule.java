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
package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class RegisterCppHeaderSetProjectionRule extends ModelActionWithInputs.ModelAction2<CppHeaderSetTag, SourceProperty> {
	private final ObjectFactory objectFactory;

	public RegisterCppHeaderSetProjectionRule(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	protected void execute(ModelNode entity, CppHeaderSetTag cSourceSetSpec, SourceProperty sourceProperty) {
		if (entity.getComponents().filter(ModelProjection.class::isInstance).map(ModelProjection.class::cast).noneMatch(it -> it.getType().equals(ModelType.of(CppHeaderSet.class)))) {
			entity.addComponent(createdUsing(ModelType.of(CppHeaderSet.class), () -> objectFactory.newInstance(DefaultCppHeaderSet.class)));
		}
	}

	public static class DefaultCppHeaderSet implements CppHeaderSet, ModelBackedLanguageSourceSetLegacyMixIn<NativeHeaderSet> {
		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(CppHeaderSet.class);
		}
	}
}
