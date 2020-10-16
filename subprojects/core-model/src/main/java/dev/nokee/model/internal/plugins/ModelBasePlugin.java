package dev.nokee.model.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.DomainObjectEventPublisherImpl;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.RealizableDomainObjectRealizerImpl;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ModelBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val eventPublisher = new DomainObjectEventPublisherImpl();
		project.getExtensions().add(DomainObjectEventPublisher.class, "__NOKEE_eventPublisher", eventPublisher);

		val realization = new RealizableDomainObjectRealizerImpl(eventPublisher);
		project.getExtensions().add(RealizableDomainObjectRealizer.class, "__NOKEE_realization", realization);
	}
}
