package dev.gradleplugins.dockit.dsl.docbook.model;

import dev.gradleplugins.dockit.dsl.docbook.DocComment;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value
@Builder(builderClassName = "Builder", setterPrefix = "with")
public class ClassDoc implements DslElementDoc {
	@NonNull
	@Getter(AccessLevel.PRIVATE)
	String className;
	@NonNull
	ClassMetaData metaData;
	@NonNull
	ClassExtensionMetaData extensionMetaData;
	@NonNull
	@Singular
	List<PropertyDoc> classProperties;
	@NonNull
	@Singular
	List<MethodDoc> classMethods;
	@NonNull
	@Singular
	List<BlockDoc> classBlocks;
	@NonNull
	@Singular
	List<ClassExtensionDoc> classExtensions;
	@NonNull
	ClassDocSuperTypes superTypes;
	@NonNull
	DocComment comment;
	@NonNull
	Map<String, String> additionalData;

	public String getId() {
		return className;
	}

	public String getName() {
		return className;
	}

	public List<ClassDoc> getInterfaces() {
		return superTypes.getInterfaces();
	}

	public String getSimpleName() {
		return StringUtils.substringAfterLast(className, ".");
	}

	@Override
	public boolean isDeprecated() {
		return metaData.isDeprecated();
	}

	@Override
	public boolean isIncubating() {
		return metaData.isIncubating();
	}

	@Override
	public boolean isReplaced() {
		return metaData.isReplaced();
	}

	@Override
	public String getReplacement() {
		return metaData.getReplacement();
	}

	Optional<ClassDoc> getSuperClass() {
		return superTypes.getSuperClass();
	}

	List<ClassDoc> getSuperTypes() {
		return superTypes.getSuperTypes();
	}

	Optional<PropertyDoc> findProperty(String name) {
		return classProperties.stream().filter(it -> it.getName().equals(name)).findFirst();
	}

	BlockDoc getBlock(String name) {
		return classBlocks.stream()
			.filter(it -> it.getName().equals(name))
			.findFirst()
			.orElseGet(() -> classExtensions.stream()
				.flatMap(it -> it.getExtensionBlocks().stream())
				.filter(it -> it.getName().equals(name))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Class " + className + " does not have a script block '" + name + "'.")));
	}

	@Override
	public String getDescription() {
		return comment.getDocbook().stream().filter(it -> it.getNodeName().equals("para")).map(Node::getTextContent).findFirst().orElse(null);
	}
}
