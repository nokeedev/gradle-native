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

package dev.nokee.language.base.internal.rules;

import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.LanguageSourcePropertySpec;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.platform.base.internal.ParentAware;
import org.gradle.api.Action;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static dev.nokee.utils.TransformerUtils.filter;

public final class SourcePropertiesExtendsFromParentRule implements Action<LanguagePropertiesAware> {

	@Override
	public void execute(LanguagePropertiesAware target) {
		if (target.instanceOf(ParentAware.class)) {
			target.getSourceProperties().withType(LanguageSourcePropertySpec.class).all(prop -> {
				prop.getSource().from((Callable<?>) () -> {
					return target.getParents().flatMap(a -> {
						return a.safeAs(LanguagePropertiesAware.class).map(b -> {
							final String name = ModelObjectIdentifiers.asFullyQualifiedName(b.getIdentifier().child(prop.getIdentifier().getName())).toString();
							return b.getSourceProperties().withType(LanguageSourcePropertySpec.class).findByName(name);
						}).map(filter(it -> !it.equals(prop))).map(Stream::of).getOrElse(Stream.empty());
					}).findFirst().map(c -> (Iterable<?>) c.getSource()).orElse(Collections.emptyList());
				});
			});
		}
	}
}
