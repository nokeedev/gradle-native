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

import java.util.List;

public interface CommandLineToolArguments extends Iterable<String> {
	static CommandLineToolArguments empty() {
		return CommandLineToolArgumentsImpl.EMPTY_ARGUMENTS;
	}

	static CommandLineToolArguments of(Object... args) {
		return of(ImmutableList.copyOf(args));
	}

	static CommandLineToolArguments of(List<Object> args) {
		if (args.isEmpty()) {
			return CommandLineToolArgumentsImpl.EMPTY_ARGUMENTS;
		}
		return new CommandLineToolArgumentsImpl(args);
	}

	List<String> get();

	final class Builder {
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
			return new CommandLineToolArgumentsImpl(delegate.build());
		}
	}
}
