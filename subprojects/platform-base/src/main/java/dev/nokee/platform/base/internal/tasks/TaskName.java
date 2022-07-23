/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.QualifyingName;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class TaskName implements ElementName {
	private static final TaskName EMPTY_TASK_NAME = new TaskName("", null);
	private final String verb;
	private final String object;

	private TaskName(String verb, String object) {
		this.verb = verb;
		this.object = object;
	}

	public String getVerb() {
		return verb;
	}

	public Optional<String> getObject() {
		return Optional.ofNullable(object);
	}

	public String get() {
		if (object == null) {
			return verb;
		}
		return verb + StringUtils.capitalize(object);
	}

	/**
	 * Used as part of {@link TaskIdentifier#ofLifecycle(DomainObjectIdentifier)}
	 *
	 * @return an empty task name, never null.
 	 */
	static TaskName empty() {
		return EMPTY_TASK_NAME;
	}

	public static TaskName lifecycle() {
		return EMPTY_TASK_NAME;
	}

	@Override
	public String toQualifiedName(QualifyingName qualifyingName) {
		return StringUtils.uncapitalize(Stream.of(verb, qualifyingName.toString(), object == null ? "" : object)
			.map(StringUtils::capitalize)
			.collect(Collectors.joining()));
	}

	public static TaskName of(String taskName) {
		requireNonNull(taskName);
		checkArgument(!taskName.isEmpty());
		checkArgument(Character.isLowerCase(taskName.charAt(0)));
		return new TaskName(taskName, null);
	}

	public static TaskName of(String verb, String object) {
		requireNonNull(verb);
		requireNonNull(object);
		checkArgument(!verb.isEmpty());
		checkArgument(!object.isEmpty());
		checkArgument(Character.isLowerCase(verb.charAt(0)));
		checkArgument(Character.isLowerCase(object.charAt(0)));
		return new TaskName(verb, object);
	}

	public static String taskName(String verb, String object) {
		return of(verb, object).get();
	}

	@Override
	public String toString() {
		return get();
	}
}
