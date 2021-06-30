package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetExtensible;
import dev.nokee.model.internal.registry.ModelConfigurer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.language.base.internal.SourceSetExtensible.discoveringInstanceOf;
import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.*;

public class ObjectiveCLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		modelConfigurer.configure(matching(discoveringInstanceOf(ObjectiveCSourceSetExtensible.class),
			once(register(sourceSet("objectiveC", ObjectiveCSourceSet.class)))));
	}
}
