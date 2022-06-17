package dev.gradleplugins.dockit.dsl.docbook.model;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.dockit.dsl.docbook.DocComment;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.dsl.source.model.PropertyMetaData;
import lombok.Value;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.List;

@Value
//@Builder(builderClassName = "Builder", setterPrefix = "with")
public class PropertyDoc implements DslElementDoc {
	ClassMetaData referringClass;
	PropertyMetaData metaData;
	DocComment comment;
	List<ExtraAttributeDoc> additionalValues;

	@Override
	public String getId() {
		return referringClass.getClassName() + ":" + metaData.getName();
	}

	public String getName() {
		return metaData.getName();
	}

	@Override
	public String getDescription() {
		return comment.getDocbook().stream().filter(it -> it.getNodeName().equals("para")).map(Node::getTextContent).findFirst().orElse(null);
	}

	@Override
	public boolean isDeprecated() {
		return metaData.isDeprecated() && !referringClass.isDeprecated();
	}

	@Override
	public boolean isIncubating() {
		return metaData.isIncubating() || metaData.getOwnerClass().isIncubating();
	}

	@Override
	public boolean isReplaced() {
		return metaData.isReplaced();
	}

	@Override
	public String getReplacement() {
		return metaData.getReplacement();
	}

	public boolean isReadOnly() {
		return !metaData.isWriteable() && !metaData.getType().getName().contains("Property");
	}

	public boolean isWriteOnly() {
		return !metaData.isProviderApi() && metaData.isWriteable();
	}

	public PropertyDoc forClass(ClassMetaData refererMetaData) {
		return forClass(refererMetaData, ImmutableList.of());
	}

	public PropertyDoc forClass(ClassMetaData refererMetaData, Collection<ExtraAttributeDoc> additionalValues) {
		if (refererMetaData == this.getReferringClass() && additionalValues.isEmpty()) {
			return this;
		}
		return new PropertyDoc(refererMetaData, metaData, comment, ImmutableList.copyOf(additionalValues));
	}
}
