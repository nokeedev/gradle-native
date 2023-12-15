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

package dev.nokee.model.internal.decorators;

import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.type.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class DeriveNameFromPropertyNameNamer implements DomainObjectNamer {
	@Override
	public ElementName determineName(Decorator.MethodMetadata details) {
		final String propertyName = propertyNameOf(details);
		final ModelType<?> returnType = ModelType.of(details.getGenericReturnType());
		if (isTaskType(returnType.getType())) {
			String taskName = propertyName;
			if (taskName.endsWith("Task")) {
				taskName = taskName.substring(0, taskName.length() - "Task".length());
			}
			return TaskName.of(taskName);
		} else if (Provider.class.isAssignableFrom(returnType.getRawType()) || Named.class.isAssignableFrom(returnType.getRawType())) {
			if (details.getAnnotations().anyMatch(it -> it.annotationType().equals(MainModelObject.class))) {
				return ElementName.ofMain(propertyName);
			} else {
				return ElementName.of(propertyName);
			}
		} else {
			return null;
		}
	}

	public static boolean isTaskType(Type type) {
		if (TaskProvider.class.isAssignableFrom(TypeToken.of(type).getRawType())) {
			return true;
		} else if (TypeToken.of(type).getType() instanceof ParameterizedType) {
			if (Task.class.isAssignableFrom((Class<?>) ((ParameterizedType) TypeToken.of(type).getType()).getActualTypeArguments()[0])) {
				return true;
			}
		}
		return false;
	}

	private static String propertyNameOf(Decorator.MethodMetadata method) {
		return StringUtils.uncapitalize(method.getName().substring("get".length()));
	}
}
