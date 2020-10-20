package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.*;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSetImpl;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class ObjectiveCppLanguageBasePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public ObjectiveCppLanguageBasePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val languageSourceSetInstantiator = project.getExtensions().getByType(LanguageSourceSetInstantiator.class);
		languageSourceSetInstantiator.registerFactory(ObjectiveCppSourceSetImpl.class, this::newObjectiveCppSourceSet);
		languageSourceSetInstantiator.registerFactoryIfAbsent(CppHeaderSetImpl.class, this::newCppHeaderSet);

		val languageSourceSetConfigurer = project.getExtensions().getByType(LanguageSourceSetConfigurer.class);
		languageSourceSetConfigurer.configureEach(ProjectIdentifier.of(project), ObjectiveCppSourceSetImpl.class, this::configureBackwardCompatibleConvention);
		languageSourceSetConfigurer.configureEach(ProjectIdentifier.of(project), CppHeaderSet.class, this::configureCppHeaders);
	}

	private void configureCppHeaders(LanguageSourceSet sourceSet) {
		sourceSet.getFilter().include("**/*.h");
	}

	private void configureBackwardCompatibleConvention(LanguageSourceSetInternal sourceSet) {
		if (sourceSet.getIdentifier().getName().get().equals("objectiveCpp")) {
			sourceSet.convention(objectFactory.fileCollection().from(ConventionalRelativeLanguageSourceSetPath.of(sourceSet.getIdentifier()), ConventionalRelativeLanguageSourceSetPath.builder().fromIdentifier(sourceSet.getIdentifier()).withSourceSetName("objcpp").build()));
		}
	}

	private ObjectiveCppSourceSetImpl newObjectiveCppSourceSet(DomainObjectIdentifier identifier) {
		return new ObjectiveCppSourceSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}

	private CppHeaderSetImpl newCppHeaderSet(DomainObjectIdentifier identifier) {
		return new CppHeaderSetImpl((LanguageSourceSetIdentifier<?>)identifier, objectFactory);
	}
}
