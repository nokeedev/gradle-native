package dev.nokee.language.jvm.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetImpl;
import dev.nokee.language.jvm.internal.JavaSourceSetImpl;
import dev.nokee.language.jvm.internal.KotlinSourceSetImpl;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.*;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.components.ComponentConfigurer;
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		project.getPlugins().withType(ComponentBasePlugin.class, a -> {
			val configurer = project.getExtensions().getByType(ComponentConfigurer.class);
			configurer.whenElementKnown(projectIdentifier, Component.class, componentIdentifier -> {
				project.getPluginManager().withPlugin("java-base", appliedPlugin -> {
					JavaJvmPluginHelper.whenSourceSetKnown(project, nameOf(componentIdentifier), newJavaSourceSet(eventPublisher, componentIdentifier, project.getObjects()));
				});

				project.getPluginManager().withPlugin("groovy-base", appliedPlugin -> {
					GroovyJvmPluginHelper.whenSourceSetKnown(project, nameOf(componentIdentifier), newGroovySourceSet(eventPublisher, componentIdentifier, project.getObjects()));
				});

				project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", appliedPlugin -> {
					KotlinJvmPluginHelper.whenSourceSetKnown(project, nameOf(componentIdentifier), newKotlinSourceSet(eventPublisher, componentIdentifier, project.getObjects()));
				});
			});
		});
	}

	private Action<SourceDirectorySet> newJavaSourceSet(DomainObjectEventPublisher eventPublisher, DomainObjectIdentifier componentIdentifier, ObjectFactory objectFactory) {
		return sourceSet -> {
			val identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("java"), JavaSourceSetImpl.class, componentIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
			eventPublisher.publish(new DomainObjectCreated<>(identifier, new JavaSourceSetImpl(identifier, sourceSet, objectFactory)));
		};
	}

	private Action<SourceDirectorySet> newGroovySourceSet(DomainObjectEventPublisher eventPublisher, DomainObjectIdentifier componentIdentifier, ObjectFactory objectFactory) {
		return sourceSet -> {
			val identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("groovy"), GroovySourceSetImpl.class, componentIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
			eventPublisher.publish(new DomainObjectCreated<>(identifier, new GroovySourceSetImpl(identifier, sourceSet, objectFactory)));
		};
	}

	private Action<SourceDirectorySet> newKotlinSourceSet(DomainObjectEventPublisher eventPublisher, DomainObjectIdentifier componentIdentifier, ObjectFactory objectFactory) {
		return sourceSet -> {
			System.out.println("CLASS " + KotlinSourceSetImpl.class.hashCode());
			val identifier = LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("kotlin"), KotlinSourceSetImpl.class, componentIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
			eventPublisher.publish(new DomainObjectCreated<>(identifier, new KotlinSourceSetImpl(identifier, sourceSet, objectFactory)));
		};
	}

	private static String nameOf(DomainObjectIdentifier identifier) {
		return ((NameAwareDomainObjectIdentifier) identifier).getName().toString();
	}
}
