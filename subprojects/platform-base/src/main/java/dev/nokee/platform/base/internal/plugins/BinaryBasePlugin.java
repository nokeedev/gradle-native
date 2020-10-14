package dev.nokee.platform.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.internal.binaries.BinaryConfigurer;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.binaries.KnownBinaryFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BinaryBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realization = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val binaryRepository = new BinaryRepository(eventPublisher, realization, project.getProviders());
		project.getExtensions().add(BinaryRepository.class, "__NOKEE_binaryRepository", binaryRepository);

		val binaryConfigurer = new BinaryConfigurer(eventPublisher);
		project.getExtensions().add(BinaryConfigurer.class, "__NOKEE_binaryConfigurer", binaryConfigurer);

		val knownBinaryFactory = new KnownBinaryFactory(() -> binaryRepository, () -> project.getExtensions().getByType(BinaryConfigurer.class));
		project.getExtensions().add(KnownBinaryFactory.class, "__NOKEE_knownBinaryFactory", knownBinaryFactory);

		val binaryViewFactory = new BinaryViewFactory(binaryRepository, binaryConfigurer);
		project.getExtensions().add(BinaryViewFactory.class, "__NOKEE_binaryViewFactory", binaryViewFactory);
	}
}
