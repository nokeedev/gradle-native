package dev.nokee.platform.nativebase.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

/**
 * Links a shared library binary from object files and imported libraries.
 *
 * @version 0.3
 */
public interface LinkSharedLibrary extends Task {
	/**
	 * The tool chain used for the compilation.
	 *
	 * @return a provider of a {@link NativeToolChain} instance of the tool chain used for the compilation, never null.
	 */
	@Internal
	Provider<NativeToolChain> getToolChain();

	/**
	 * <em>Additional</em> arguments to provide to the linker.
	 *
	 * It act as an escape hatch to the current model.
	 * Please open an issue on https://github.com/nokeedev/gradle-native with your reason for using this hatch so we can improve the model.
	 *
	 * @return a property for adding additional arguments, never null.
	 */
	@Internal
	ListProperty<String> getLinkerArgs();

	/**
	 * Returns the location of the linked binary.
	 *
	 * @return a provider of where the binary is linked.
	 */
	@Internal
	Provider<RegularFile> getLinkedFile();
}
