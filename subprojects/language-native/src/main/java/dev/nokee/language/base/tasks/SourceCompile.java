package dev.nokee.language.base.tasks;

import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.platform.base.ToolChain;

public interface SourceCompile extends Task {
	/**
	 * The tool chain used for the compilation.
	 *
	 * @return a provider of a {@link ToolChain} instance of the tool chain used for the compilation, never null.
	 */
	@Internal
	Provider<? extends ToolChain> getToolChain();

	/**
	 * <em>Additional</em> arguments to provide to the compiler.
	 *
	 * It act as an escape hatch to the current model.
	 * Please open an issue on https://github.com/nokeedev/gradle-native with your reason for using this hatch so we can improve the model.
	 *
	 * @return a property for adding additional arguments, never null.
	 */
	@Input
	ListProperty<String> getCompilerArgs();
}
