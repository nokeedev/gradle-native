package dev.nokee.docs.dsl.docbook;

import dev.nokee.docs.dsl.docbook.model.ClassDoc;
import dev.nokee.docs.dsl.docbook.model.ClassDocSuperTypes;
import dev.nokee.docs.dsl.docbook.model.ClassExtensionMetaData;
import dev.nokee.docs.dsl.source.TypeNameResolver;
import dev.nokee.docs.dsl.source.model.ClassMetaData;
import dev.nokee.docs.model.ClassMetaDataRepository;
import groovy.json.JsonSlurper;
import lombok.Value;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DslDocModel {
	private final File classDocbookDir;
	private final Map<String, ClassDoc> classes = new HashMap<>();
	private final ClassMetaDataRepository<ClassMetaData> classMetaData;
	private final Map<String, ClassExtensionMetaData> extensionMetaData;
	private final LinkedList<String> currentlyBuilding = new LinkedList<String>();

	public DslDocModel(File classDocbookDir, ClassMetaDataRepository<ClassMetaData> classMetaData, Map<String, ClassExtensionMetaData> extensionMetaData) {
		this.classDocbookDir = classDocbookDir;
		this.classMetaData = classMetaData;
		this.extensionMetaData = extensionMetaData;
	}

	public Collection<ClassDoc> getClasses() {
		return classes.values().stream().filter(it -> !it.getName().contains(".internal.")).collect(Collectors.toList());
	}

	public boolean isKnownType(String className) {
		return classMetaData.find(className) != null;
	}

	public ClassDoc findClassDoc(String className) {
		ClassDoc classDoc = classes.get(className);
		if (classDoc == null && getFileForClass(className).isFile()) {
			return getClassDoc(className);
		}
		return classDoc;
	}

	public ClassDoc getClassDoc(String className) {
		ClassDoc classDoc = classes.get(className);
		if (classDoc == null) {
			classDoc = loadClassDoc(className);
			classes.put(className, classDoc);
			new ReferencedTypeBuilder(this).build(classDoc);
		}
		return classDoc;
	}

	private ClassDoc loadClassDoc(String className) {
		if (currentlyBuilding.contains(className)) {
			throw new RuntimeException(String.format("Cycle building %s. Currently building %s", className, currentlyBuilding));
		}
		currentlyBuilding.addLast(className);
		try {
			ClassMetaData classMetaData = this.classMetaData.find(className);
			if (classMetaData == null) {
				if (!className.contains(".internal.")) {
					throw new RuntimeException(String.format("No meta-data found for class '%s'.", className));
				}
				classMetaData = new ClassMetaData(className);
			}
			try {
				ClassExtensionMetaData extensionMetaData = this.extensionMetaData.get(className);
				if (extensionMetaData == null) {
					extensionMetaData = new ClassExtensionMetaData(className);
				}
				File classFile = getFileForClass(className);
				if (!classFile.isFile()) {
					throw new RuntimeException(String.format("Docbook source file not found for class '%s' in %s.", className, classDocbookDir));
				}

				// Build the ClassDoc
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				JavadocConverter javadocConverter = new JavadocConverter(document, new JavadocLinkConverter(document, new TypeNameResolver(this.classMetaData), new DocLinkBuilder(this), this.classMetaData));
				ClassDoc.Builder builder = ClassDoc.builder()
					.withClassName(className)
					.withMetaData(classMetaData)
					.withExtensionMetaData(extensionMetaData)
					.withSuperTypes(ClassDocSuperTypes.of(classMetaData, this))
					.withComment(javadocConverter.parse(classMetaData, DefaultGenerationListener.INSTANCE));

				ClassDocBuilder docBuilder = new ClassDocBuilder(this, javadocConverter, classMetaData, ClassFile.parse(classFile), extensionMetaData);
				docBuilder.build(builder);

				return builder.build();
			} catch (ClassDocGenerationException e) {
				throw e;
			} catch (Exception e) {
				throw new ClassDocGenerationException(String.format("Could not load the class documentation for class '%s'.", className), e);
			}
		} finally {
			currentlyBuilding.removeLast();
		}
	}

	private File getFileForClass(String className) {
		return new File(classDocbookDir, String.format("%s.json", className));
	}

	@Value
	static class ClassFile {
		List<String> properties;
		List<String> methods;

		static ClassFile parse(File classFile) {
			Map<Object, Object> v = (Map<Object, Object>) new JsonSlurper().parse(classFile);
			return new ClassFile((List<String>) v.get("properties"), (List<String>) v.get("methods"));
		}
	}


}
