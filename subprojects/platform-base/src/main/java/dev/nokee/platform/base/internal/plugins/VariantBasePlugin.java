package dev.nokee.platform.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.internal.variants.*;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VariantBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realization = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val variantRepository = new VariantRepository(eventPublisher, realization, project.getProviders());
		project.getExtensions().add(VariantRepository.class, "__NOKEE_variantRepository", variantRepository);

		val variantConfigurer = new VariantConfigurer(eventPublisher);
		project.getExtensions().add(VariantConfigurer.class, "__NOKEE_variantConfigurer", variantConfigurer);

		val knownVariantFactory = new KnownVariantFactory(() -> variantRepository, () -> variantConfigurer);
		project.getExtensions().add(KnownVariantFactory.class, "__NOKEE_knownVariantFactory", knownVariantFactory);

		val variantViewFactory = new VariantViewFactory(variantRepository, variantConfigurer, knownVariantFactory);
		project.getExtensions().add(VariantViewFactory.class, "__NOKEE_variantViewFactory", variantViewFactory);
	}
}
