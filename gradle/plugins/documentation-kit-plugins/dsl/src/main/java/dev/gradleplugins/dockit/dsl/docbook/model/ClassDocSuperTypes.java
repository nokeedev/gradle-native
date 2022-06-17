package dev.gradleplugins.dockit.dsl.docbook.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.gradleplugins.dockit.dsl.docbook.DslDocModel;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import lombok.*;

import java.util.List;
import java.util.Optional;

@Value
@Builder(builderClassName = "Builder", setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassDocSuperTypes {
	ClassDoc superClass;
	@Singular
	@NonNull
	List<ClassDoc> interfaces;

	public Optional<ClassDoc> getSuperClass() {
		return Optional.ofNullable(superClass);
	}

	public List<ClassDoc> getSuperTypes() {
		return ImmutableList.copyOf(Iterables.concat(getSuperClass().map(ImmutableList::of).orElse(ImmutableList.of()), interfaces));
	}

	public static ClassDocSuperTypes of(ClassMetaData metaData, DslDocModel model) {
		val builder = builder();

		String superClassName = metaData.getSuperClassName();
		if (superClassName != null) {
			// Assume this is a class and so has implemented all properties and methods somewhere in the superclass hierarchy
			ClassDoc superClass = model.getClassDoc(superClassName);
			builder.withSuperClass(superClass);
		}

		List<String> interfaceNames = metaData.getInterfaceNames();
		for (String interfaceName : interfaceNames) {
			ClassDoc superInterface = model.findClassDoc(interfaceName);
			if (superInterface != null) {
				builder.withInterface(superInterface);
			}
		}

		return builder.build();
	}
}
