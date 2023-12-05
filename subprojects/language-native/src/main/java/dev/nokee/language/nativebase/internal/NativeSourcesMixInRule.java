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

package dev.nokee.language.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.SourcePropertyName;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;

import java.util.List;
import java.util.function.Function;

public final class NativeSourcesMixInRule<TargetType> implements Action<TargetType> {
	private final List<Spec> specs;

	public NativeSourcesMixInRule(Spec... specs) {
		this.specs = ImmutableList.copyOf(specs);
	}

	@Override
	public void execute(TargetType target) {
		for (Spec spec : specs) {
			spec.execute(target);
		}
	}

	public static final class Spec {
		private final SourcePropertyName propertyName;
		private final Class<?> type;
		private final Function<Object, ConfigurableFileCollection> mapper;
		private final ObjectFactory objects;

		@SuppressWarnings("unchecked")
		public <T> Spec(SourcePropertyName propertyName, Class<T> type, Function<T, ConfigurableFileCollection> mapper, ObjectFactory objects) {
			this.propertyName = propertyName;
			this.type = type;
			this.mapper = (Function<Object, ConfigurableFileCollection>) mapper;
			this.objects = objects;
		}

		public void execute(Object target) {
			ConfigurableFileCollection sources = null;
			if (target instanceof NativeSourcesAware) {
				sources = objects.fileCollection();
			} else if (type.isInstance(target)) {
				sources = mapper.apply(type.cast(target));
			}

			if (sources != null) {
				((ExtensionAware) target).getExtensions().add(ConfigurableFileCollection.class, propertyName.asExtensionName(), sources);
			}
		}
	}
}
