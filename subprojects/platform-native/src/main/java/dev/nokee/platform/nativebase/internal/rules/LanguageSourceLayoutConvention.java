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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import org.gradle.api.file.ConfigurableFileCollection;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public final class LanguageSourceLayoutConvention extends ModelActionWithInputs.ModelAction3<SourcePropertyComponent, ParentComponent, ElementNameComponent> {
	@Override
	protected void execute(ModelNode entity, SourcePropertyComponent source, ParentComponent parent, ElementNameComponent elementName) {
		((ConfigurableFileCollection) source.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
			return ParentUtils.stream(parent).map(it -> it.find(FullyQualifiedNameComponent.class)).filter(Optional::isPresent).map(Optional::get).map(FullyQualifiedNameComponent::get).map(it -> "src/" + it + "/" + elementName.get()).collect(Collectors.toList());
		});
	}
}
