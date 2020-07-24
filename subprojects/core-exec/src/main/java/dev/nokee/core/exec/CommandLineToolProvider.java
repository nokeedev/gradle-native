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
