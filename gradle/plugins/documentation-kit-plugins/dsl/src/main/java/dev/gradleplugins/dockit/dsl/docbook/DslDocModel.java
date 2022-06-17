package dev.gradleplugins.dockit.dsl.docbook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.gradleplugins.dockit.dsl.docbook.model.ClassDoc;
import dev.gradleplugins.dockit.dsl.docbook.model.ClassDocSuperTypes;
import dev.gradleplugins.dockit.dsl.docbook.model.ClassExtensionMetaData;
import dev.gradleplugins.dockit.dsl.source.TypeNameResolver;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.model.ClassMetaDataRepository;
import lombok.Value;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DslDocModel {
	private final List<File> classDocbookDirs;
	private final Map<String, ClassDoc> classes = new HashMap<>();
	private final ClassMetaDataRepository<ClassMetaData> classMetaData;
	private final Map<String, ClassExtensionMetaData> extensionMetaData;
	private final LinkedList<String> currentlyBuilding = new LinkedList<String>();

	public DslDocModel(Set<File> classDocbookDirs, ClassMetaDataRepository<ClassMetaData> classMetaData, Map<String, ClassExtensionMetaData> extensionMetaData) {
		this.classDocbookDirs = ImmutableList.copyOf(classDocbookDirs);
		this.classMetaData = classMetaData;
		this.extensionMetaData = extensionMetaData;
	}

	public Collection<ClassDoc> getClasses() {
		return classes.values().stream().filter(it -> !it.getName().contains(".internal.")).collect(Collectors.toList());
	}

	public Set<String> getKeys() {
		return classes.keySet();
	}

	public boolean isKnownType(String className) {
		return classMetaData.find(className) != null;
	}

	public ClassDoc findClassDoc(String className) {
		ClassDoc classDoc = classes.get(className);
		if (classDoc == null && getFileForClass(className).isPresent()) {
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
				Optional<File> classFile = getFileForClass(className);
				if (!classFile.isPresent()) {
					throw new RuntimeException(String.format("Docbook source file not found for class '%s' in %s.", className, classDocbookDirs));
				}
				// Build the ClassDoc
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				JavadocConverter javadocConverter = new JavadocConverter(document, new JavadocLinkConverter(document, new TypeNameResolver(this.classMetaData), new DocLinkBuilder(this), this.classMetaData));
				ClassDoc.Builder builder = ClassDoc.builder()
					.withClassName(className)
					.withMetaData(classMetaData)
					.withExtensionMetaData(extensionMetaData)
					.withSuperTypes(ClassDocSuperTypes.of(classMetaData, this))
					.withComment(javadocConverter.parse(classMetaData, new DefaultGenerationListener()));

				ClassFile configurationFile = ClassFile.parse(classFile.get());
				builder.withAdditionalData(ImmutableMap.of("category", configurationFile.category));

				ClassDocBuilder docBuilder = new ClassDocBuilder(this, javadocConverter, classMetaData, configurationFile, extensionMetaData);
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

	private Optional<File> getFileForClass(String className) {
		return classDocbookDirs.stream()
			.map(classDocbookDir -> new File(classDocbookDir, String.format("%s.json", className)))
			.filter(File::isFile)
			.findFirst();
	}

	@Value
	static class ClassFile {
		String category;
		List<String> properties;
		List<String> methods;

		static ClassFile parse(File classFile) {
			try (Reader reader = new FileReader(classFile)) {
				Map<Object, Object> v = new Gson().fromJson(reader, new TypeToken<Map<Object, Object>>() {}.getType());
				return new ClassFile((String) v.get("category"), (List<String>) v.get("properties"), (List<String>) v.get("methods"));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
