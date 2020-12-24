package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSetExtensible;
import dev.nokee.model.internal.registry.ModelConfigurer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import static dev.nokee.language.base.internal.SourceSetExtensible.discoveringInstanceOf;
import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.*;

public class CppLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CppLanguageBasePlugin.class);
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		modelConfigurer.configure(matching(discoveringInstanceOf(CppSourceSetExtensible.class), once(register(sourceSet("cpp", CppSourceSet.class)))));
	}
}
