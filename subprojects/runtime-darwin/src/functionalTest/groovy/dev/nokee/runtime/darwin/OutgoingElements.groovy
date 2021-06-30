package dev.nokee.runtime.darwin

import org.apache.commons.io.FilenameUtils
import org.gradle.api.attributes.Usage

import java.util.function.Consumer

import static org.apache.commons.io.FilenameUtils.separatorsToUnix

final class OutgoingElements {
	private final String name
	private final List<String> segment = new ArrayList<>()

	OutgoingElements(String name) {
		this.name = name
		segment << """
		|configurations.create('${name}') {
		|	canBeConsumed = true
		|	canBeResolved = false
		|}
		|""".stripMargin()
	}

	OutgoingElements asFramework() {
		segment << """
			|configurations.${name} {
			|	attributes {
			|		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.C_PLUS_PLUS_API}+${Usage.NATIVE_LINK}'))
			|		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
			|		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
			|	}
			|}
			|""".stripMargin()
		return this
	}

	OutgoingElements asHeaders() {
		segment << """
			|configurations.${name} {
			|	attributes {
			|		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.C_PLUS_PLUS_API}'))
			|		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.HEADERS_CPLUSPLUS))
			|		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
			|	}
			|}
			|""".stripMargin()
		return this
	}

	OutgoingElements mainVariant(Consumer<? super OutgoingArtifact> builder) {
		def artifact = new OutgoingArtifact()
		builder.accept(artifact)
		segment << """
			|configurations.${name} {
			|	outgoing {
			|		${artifact.toString()}
			|	}
			|}
			|""".stripMargin()
		return this
	}

	OutgoingElements variant(String variantName, Consumer<? super OutgoingArtifact> builder) {
		def artifact = new OutgoingArtifact()
		builder.accept(artifact)
		segment << """
			|configurations.${name} {
			|	outgoing.variants.create('${variantName}') {
			|		${artifact.toString()}
			|	}
			|}
			|""".stripMargin()
		return this
	}

	@Override
	String toString() {
		return segment.join('\n')
	}

	private static class OutgoingArtifact {
		private String artifactType
		private File location

		OutgoingArtifact artifact(File location) {
			artifactType = FilenameUtils.getExtension(location.getName())
			this.location = location
			return this
		}

		OutgoingArtifact type(String artifactType) {
			this.artifactType = artifactType
			return this
		}

		@Override
		String toString() {
			return """
				|artifact(file('${separatorsToUnix(location.absolutePath)}')) {
				|	type = '${artifactType}'
				|}
				|""".stripMargin()
		}
	}

	static OutgoingElements outgoingElements(String name = 'testElements') {
		return new OutgoingElements(name)
	}
	static OutgoingElements outgoingFrameworkElements(String name = 'testElements') {
		return outgoingElements(name).asFramework()
	}
	static OutgoingElements outgoingHeadersElements(String name = 'testElements') {
		return outgoingElements(name).asHeaders()
	}
}
