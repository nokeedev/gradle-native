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
package dev.nokee.platform.base.internal;

import com.google.common.collect.Streams;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Namer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TaskNamer implements Namer<TaskIdentifier<?>> {
	public static final TaskNamer INSTANCE = new TaskNamer();

	@Override
	public String determineName(TaskIdentifier<?> identifier) {
		// Uncapitalize to support TaskIdentifier.ofLifecycle(...) which should be phase out in the future
		return StringUtils.uncapitalize(identifier.getName().getVerb() + Streams.stream(identifier)
			.flatMap(it -> {
				if (it instanceof ProjectIdentifier) {
					return Stream.empty();
				} else if (it instanceof ComponentIdentifier) {
					if (((ComponentIdentifier) it).isMainComponent()) {
						return Stream.empty();
					} else {
						return Stream.of(((ComponentIdentifier) it).getName().get());
					}
				} else if (it instanceof ComponentIdentity) {
					if (((ComponentIdentity) it).isMainComponent()) {
						return Stream.empty();
					} else {
						return Stream.of(((ComponentIdentity) it).getName().get());
					}
				} else if (it instanceof VariantIdentifier) {
					return Stream.of(((VariantIdentifier) it).getUnambiguousName());
				} else if (it instanceof BinaryIdentity) {
					return Stream.of((BinaryIdentity) it).filter(t -> !t.isMain()).map(t -> t.getName().toString());
				} else if (it instanceof HasName) {
					val name = ((HasName) it).getName();
					if (name instanceof TaskName) {
						return Streams.stream(((TaskName) name).getObject());
					} else {
						return Stream.of(name.toString());
					}
				} else {
					throw new UnsupportedOperationException();
				}
			})
			.map(StringUtils::capitalize)
			.collect(Collectors.joining()));
	}
}
