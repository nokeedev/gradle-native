package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.cpp.internal.CppHeaderSetImpl;
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport;
import dev.nokee.language.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.language.nativebase.internal.rules.RegisterNativeLanguageSourceSetRule;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

import static dev.nokee.model.internal.DomainObjectDiscovered.discoveredType;

public class ObjectiveCppLanguagePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public ObjectiveCppLanguagePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ObjectiveCppLanguageBasePlugin.class);

		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("objectiveCpp"), ObjectiveCppSourceSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("headers"), CppHeaderSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
	}
}
