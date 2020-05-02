package dev.nokee.language.nativebase.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

import java.util.Set;

/**
 * Compiles native source files into object files.
 *
 * @version 0.3
 */
public interface NativeSourceCompile extends Task {
	/**
	 * The tool chain used for the compilation.
	 *
	 * @return a provider of a {@link NativeToolChain} instance of the tool chain used for the compilation, never null.
	 */
	@Internal
	Provider<NativeToolChain> getToolChain();

	/**
	 * <em>Additional</em> arguments to provide to the compiler.
	 *
	 * It act as an escape hatch to the current model.
	 * Please open an issue on https://github.com/nokeedev/gradle-native with your reason for using this hatch so we can improve the model.
	 *
	 * @return a property for adding additional arguments, never null.
	 */
	@Internal
	ListProperty<String> getCompilerArgs();

	/**
	 * Returns the header search paths used during the compilation.
	 *
	 * @return a provider of search path entries, never null.
	 */
	@Internal
	Provider<Set<HeaderSearchPath>> getHeaderSearchPaths();
}
