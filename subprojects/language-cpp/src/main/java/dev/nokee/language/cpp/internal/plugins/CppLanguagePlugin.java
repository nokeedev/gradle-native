package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.cpp.internal.CppHeaderSetImpl;
import dev.nokee.language.cpp.internal.CppSourceSetImpl;
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport;
import dev.nokee.language.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.language.nativebase.internal.rules.RegisterNativeLanguageSourceSetRule;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.runtime.nativebase.internal.plugins.DomainKnowledgeToolchainsRules;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import static dev.nokee.model.internal.DomainObjectDiscovered.discoveredType;

public class CppLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CppLanguageBasePlugin.class);

		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);
		project.getPluginManager().apply(DomainKnowledgeToolchainsRules.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("cpp"), CppSourceSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("headers"), CppHeaderSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
	}
}
