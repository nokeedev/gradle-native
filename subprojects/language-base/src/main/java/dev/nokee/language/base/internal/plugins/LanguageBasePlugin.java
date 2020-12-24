package dev.nokee.language.base.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.BridgedLanguageSourceSetProjection;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public class LanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		DefaultImporter.forProject(project).defaultImport(LanguageSourceSet.class);
	}

	public static <T extends LanguageSourceSet> NodeRegistration<T> sourceSet(String name, Class<T> publicType) {
		return NodeRegistration.of(name, of(publicType))
			.withProjection(managed(of(BaseLanguageSourceSetProjection.class)));
	}

	public static <T extends LanguageSourceSet> NodeRegistration<T> bridgeSourceSet(SourceDirectorySet from, Class<T> to) {
		return NodeRegistration.of(from.getName(), of(to))
			.withProjection(managed(of(BridgedLanguageSourceSetProjection.class), from));
	}
}
