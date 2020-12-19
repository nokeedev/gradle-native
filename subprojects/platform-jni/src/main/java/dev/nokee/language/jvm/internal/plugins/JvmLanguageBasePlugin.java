package dev.nokee.language.jvm.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetImpl;
import dev.nokee.language.jvm.internal.JavaSourceSetImpl;
import dev.nokee.language.jvm.internal.KotlinSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.*;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		project.getPlugins().withType(ComponentModelBasePlugin.class, a -> {
			val components = project.getExtensions().getByType(ComponentContainer.class);
			components.whenElementKnownEx(Component.class, knownComponent -> {
				// TODO: Because identifier are quite strict, we have to poke the model node...
				Class<? extends Component> type = project.getExtensions().getByType(ModelLookup.class).get(((ModelIdentifier<?>)knownComponent.getIdentifier()).getPath()).get(Component.class).getClass();
				val componentIdentifier = ComponentIdentifier.of(ComponentName.of(((ModelIdentifier<?>)knownComponent.getIdentifier()).getPath().getName()), type, projectIdentifier);
				project.getPluginManager().withPlugin("java-base", appliedPlugin -> {
					JavaJvmPluginHelper.whenSourceSetKnown(project, nameOf(componentIdentifier), newJavaSourceSet(eventPublisher, componentIdentifier, project.getObjects(), project.getLayout()));
				});

				project.getPluginManager().withPlugin("groovy-base", appliedPlugin -> {
					GroovyJvmPluginHelper.whenSourceSetKnown(project, nameOf(componentIdentifier), newGroovySourceSet(eventPublisher, componentIdentifier, project.getObjects(), project.getLayout()));
				});

				project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", appliedPlugin -> {
					KotlinJvmPluginHelper.whenSourceSetKnown(project, nameOf(componentIdentifier), newKotlinSourceSet(eventPublisher, componentIdentifier, project.getObjects(), project.getLayout()));
				});
			});
		});
	}

	private Action<SourceDirectorySet> newJavaSourceSet(DomainObjectEventPublisher eventPublisher, DomainObjectIdentifier componentIdentifier, ObjectFactory objectFactory, ProjectLayout projectLayout) {
		return sourceSet -> {
			val identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("java"), JavaSourceSetImpl.class, componentIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
			eventPublisher.publish(new DomainObjectCreated<>(identifier, new JavaSourceSetImpl(identifier, sourceSet, objectFactory, projectLayout)));
		};
	}

	private Action<SourceDirectorySet> newGroovySourceSet(DomainObjectEventPublisher eventPublisher, DomainObjectIdentifier componentIdentifier, ObjectFactory objectFactory, ProjectLayout projectLayout) {
		return sourceSet -> {
			val identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("groovy"), GroovySourceSetImpl.class, componentIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
			eventPublisher.publish(new DomainObjectCreated<>(identifier, new GroovySourceSetImpl(identifier, sourceSet, objectFactory, projectLayout)));
		};
	}

	private Action<SourceDirectorySet> newKotlinSourceSet(DomainObjectEventPublisher eventPublisher, DomainObjectIdentifier componentIdentifier, ObjectFactory objectFactory, ProjectLayout projectLayout) {
		return sourceSet -> {
			val identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("kotlin"), KotlinSourceSetImpl.class, componentIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
			eventPublisher.publish(new DomainObjectCreated<>(identifier, new KotlinSourceSetImpl(identifier, sourceSet, objectFactory, projectLayout)));
		};
	}

	private static String nameOf(DomainObjectIdentifier identifier) {
		return ((NameAwareDomainObjectIdentifier) identifier).getName().toString();
	}
}
