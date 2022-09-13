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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class CommandLineToolArguments implements Iterable<String> {
	private static final CommandLineToolArguments EMPTY_ARGUMENTS = new CommandLineToolArguments();

	private final ImmutableList<Object> arguments;

	public CommandLineToolArguments() {
		this(ImmutableList.of());
	}

	public CommandLineToolArguments(List<Object> arguments) {
		this.arguments = ImmutableList.copyOf(arguments);
	}

	public static CommandLineToolArguments empty() {
		return EMPTY_ARGUMENTS;
	}

	public static CommandLineToolArguments of(Object... args) {
		return of(ImmutableList.copyOf(args));
	}

	public static CommandLineToolArguments of(List<Object> args) {
		if (args.isEmpty()) {
			return EMPTY_ARGUMENTS;
		}
		return new CommandLineToolArguments(args);
	}

	public List<String> get() {
		return ImmutableList.copyOf(arguments.stream().map(Object::toString).collect(Collectors.toList()));
	}

	public String toString() {
		if (arguments.isEmpty()) {
			return "arguments [<empty>]";
		} else {
			return "arguments [" + arguments.stream().map(it -> "'" + it + "'").collect(Collectors.joining(", ")) + "]";
		}
	}

	@Override
	public Iterator<String> iterator() {
		return get().iterator();
	}

	public static final class Builder {
		private final ImmutableList.Builder<Object> delegate = ImmutableList.builder();

		/**
		 * Adds the specified argument.
		 *
		 * @param argument  the argument to add, must not be null
		 * @return this builder, never null
		 */
		public Builder arg(Object argument) {
			delegate.add(argument);
			return this;
		}

		/**
		 * Adds the specified arguments.
		 *
		 * @param arguments  the arguments to add, must not be null
		 * @return this builder, never null
		 */
		public Builder args(Object... arguments) {
			delegate.add(arguments);
			return this;
		}

		/**
		 * Adds the specified arguments.
		 *
		 * @param arguments  the arguments to add, must not be null
		 * @return this builder, never null
		 */
		public Builder args(Iterable<Object> arguments) {
			delegate.addAll(arguments);
			return this;
		}

		/**
		 * Creates tool arguments for this builder.
		 *
		 * @return the arguments, never null
		 */
		public CommandLineToolArguments build() {
			return new CommandLineToolArguments(delegate.build());
		}
	}
}
