package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.c.internal.CSourceSetImpl;
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

public class CLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CLanguageBasePlugin.class);

		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);
		project.getPluginManager().apply(DomainKnowledgeToolchainsRules.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("c"), CSourceSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("headers"), CHeaderSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
	}
}
