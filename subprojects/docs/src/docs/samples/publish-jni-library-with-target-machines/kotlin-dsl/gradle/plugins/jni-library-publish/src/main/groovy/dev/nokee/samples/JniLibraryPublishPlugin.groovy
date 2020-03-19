package dev.nokee.samples

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily

import javax.inject.Inject;

abstract class JniLibraryPublishPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		ConfigurationContainer configurations = project.getConfigurations()
		ObjectFactory objects = project.getObjects()
		TaskContainer tasks = project.getTasks()
		DependencyHandler dependencies = project.getDependencies()
		/*dev.nokee.platform.jni.JniLibraryExtension*/def extension = extension(project)
		SoftwareComponentContainer components = project.getComponents()

		Configuration library = configurations.create("library") { Configuration config ->
			config.canBeConsumed = false
			config.canBeResolved = true
			config.attributes { it.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME)) }
			project.afterEvaluate { config.outgoing.artifact(tasks.named("jar", Jar).flatMap { it.archiveFile }) }
		}
		AdhocComponentWithVariants jni = softwareComponentFactory.adhoc("jni")
		jni.addVariantsFromConfiguration(library) { it.mapToMavenScope("runtime") }
		components.add(jni);



		// Resolve all
		project.afterEvaluate {
			extension.targetMachines.get().each { targetMachine ->
				dependencies.add(library.name, "${project.group}:${project.name}-${variantName(targetMachine)}:${project.version}")

				Configuration libraryVariant = configurations.create("library" + variantName(targetMachine).capitalize()) { Configuration config ->
					config.canBeConsumed = false
					config.canBeResolved = true
					config.attributes { AttributeContainer attributes ->
						attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
						attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EMBEDDED))
						attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
						attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
						attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(OperatingSystemFamily, toOperatingSystemFamily(targetMachine)))
						attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
					config.getOutgoing().artifact(tasks.named("jar", Jar).flatMap { it.archiveFile })
				}
				jni.addVariantsFromConfiguration(libraryVariant) { it.mapToMavenScope("runtime") }
				dependencies.add(libraryVariant.name, "${project.group}:${project.name}-${variantName(targetMachine)}:${project.version}")


				Configuration jniLibraryVariant = configurations.create("jniLibrary" + variantName(targetMachine).capitalize()) { Configuration config ->
					config.canBeConsumed = false
					config.canBeResolved = true
					config.attributes { AttributeContainer attributes ->
						attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
						attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling, Bundling.EMBEDDED))
						attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
						attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.JAVA_RUNTIME))
						attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(OperatingSystemFamily, toOperatingSystemFamily(targetMachine)))
						attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture, MachineArchitecture.X86_64))
					}
				}
				AdhocComponentWithVariants jniVariant = softwareComponentFactory.adhoc("jni${variantName(targetMachine).capitalize()}")
				// FIXME: Introduce APIs to avoid using getVariantCollection() internal API
				def jniLibraries = extension.variantCollection.matching { it.targetMachine.equals(targetMachine) }
				if (!jniLibraries.isEmpty()) {
					/*dev.nokee.platform.jni.JniLibrary*/def jniLibrary = jniLibraries.iterator().next()
					jniLibraryVariant.outgoing.artifact(jniLibrary.jar.jarTask.flatMap { it.archiveFile })
					jniVariant.addVariantsFromConfiguration(jniLibraryVariant) { it.mapToMavenScope("runtime") }
				}
				components.add(jniVariant);
			}
		}
	}

	/*dev.nokee.platform.jni.JniLibraryExtension*/def extension(Project project) {
		return project.extensions.library
	}

	String variantName(/*dev.nokee.platform.nativebase.TargetMachine*/ targetMachine) {
		if (targetMachine.operatingSystemFamily.linux) {
			return "linux";
		} else if (targetMachine.operatingSystemFamily.macOs) {
			return "macos";
		} else if (targetMachine.operatingSystemFamily.windows) {
			return "windows";
		} else {
			throw new UnsupportedOperationException()
		}
	}

	String toOperatingSystemFamily(/*dev.nokee.platform.nativebase.TargetMachine*/ targetMachine) {
		if (targetMachine.operatingSystemFamily.linux) {
			return OperatingSystemFamily.LINUX;
		} else if (targetMachine.operatingSystemFamily.macOs) {
			return OperatingSystemFamily.MACOS;
		} else if (targetMachine.operatingSystemFamily.windows) {
			return OperatingSystemFamily.WINDOWS;
		} else {
			throw new UnsupportedOperationException()
		}
	}

	@Inject
	protected abstract SoftwareComponentFactory getSoftwareComponentFactory();
}
