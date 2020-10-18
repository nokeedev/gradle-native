package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.internal.ConventionalRelativeLanguageSourceSetPath;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.c.internal.CHeaderSetImpl;
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport;
import dev.nokee.language.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.language.nativebase.internal.rules.RegisterNativeLanguageSourceSetRule;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;

import javax.inject.Inject;

import static dev.nokee.model.internal.DomainObjectDiscovered.discoveredType;

public class ObjectiveCLanguagePlugin implements Plugin<Project> {
	private final ObjectFactory objectFactory;

	@Inject
	public ObjectiveCLanguagePlugin(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);

		project.getPluginManager().apply(StandardToolChainsPlugin.class);
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("objectiveC"), ObjectiveCSourceSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class), this::configureConvention)));
		eventPublisher.subscribe(discoveredType(HasNativeLanguageSupport.class, new RegisterNativeLanguageSourceSetRule(LanguageSourceSetName.of("headers"), CHeaderSetImpl.class, project.getExtensions().getByType(LanguageSourceSetRegistry.class))));
	}

	private void configureConvention(LanguageSourceSetInternal sourceSet) {
		sourceSet.convention(objectFactory.fileCollection().from(ConventionalRelativeLanguageSourceSetPath.of(sourceSet.getIdentifier()), ConventionalRelativeLanguageSourceSetPath.builder().fromIdentifier(sourceSet.getIdentifier()).withSourceSetName("objc").build()));
	}
}
