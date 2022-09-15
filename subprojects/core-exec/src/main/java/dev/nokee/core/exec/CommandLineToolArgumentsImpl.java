/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@RequiredArgsConstructor
final class CommandLineToolArgumentsImpl implements CommandLineToolArguments {
	public static final CommandLineToolArgumentsImpl EMPTY_ARGUMENTS = new CommandLineToolArgumentsImpl(ImmutableList.of());

	private final List<Object> arguments;

	@Override
	public List<String> get() {
		return ImmutableList.copyOf(arguments.stream().map(Object::toString).collect(Collectors.toList()));
	}
}
