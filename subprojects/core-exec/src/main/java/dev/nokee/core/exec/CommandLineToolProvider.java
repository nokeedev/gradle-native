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

import dev.nokee.core.exec.internal.FixedCommandLineToolProvider;
import dev.nokee.core.exec.internal.SupplierCommandLineToolProvider;
import dev.nokee.core.exec.internal.UnavailableCommandLineToolProvider;

import java.util.function.Supplier;

/**
 * A command line tool provider if available.
 *
 * @since 0.5
 */
public interface CommandLineToolProvider {
	/**
	 * Returns the command line tool provided by this provider.
	 *
	 * @return a {@link CommandLineTool} instance or throws {@link IllegalArgumentException} if no command line tool can be provided.
	 */
	CommandLineTool get();

	/**
	 * Returns the availability of the command line tool providered.
	 * @return {@code true} if the command line tool is available or {@code false} otherwise.
	 */
	boolean isAvailable();

	/**
	 * Creates a fixed provider of the specified command line tool.
	 * Calling {@link CommandLineToolProvider#get()} will return the specified command line tool.
	 *
	 * @param tool a command line tool to provide.
	 * @return a {@link CommandLineToolProvider} instance for the specified tool
	 */
	static CommandLineToolProvider of(CommandLineTool tool) {
		return new FixedCommandLineToolProvider(tool);
	}

	/**
	 * Creates an unavailable provider.
	 * Calling {@link CommandLineToolProvider#get()} will throw an exception.
	 *
	 * @return a {@link CommandLineToolProvider} instance that is unavailable.
	 */
	static CommandLineToolProvider unavailable() {
		return new UnavailableCommandLineToolProvider();
	}

	/**
	 * Creates a provider from the specified command line tool supplier.
	 * Calling {@link CommandLineToolProvider#get()} will throw an exception only if the supplier returns null.
	 *
	 * @param toolSupplier a supplier for the tool provided.
	 * @return a {@link CommandLineToolProvider} instance for the specified tool supplier.
	 */
	static CommandLineToolProvider from(Supplier<CommandLineTool> toolSupplier) {
		return new SupplierCommandLineToolProvider(toolSupplier);
	}
}
