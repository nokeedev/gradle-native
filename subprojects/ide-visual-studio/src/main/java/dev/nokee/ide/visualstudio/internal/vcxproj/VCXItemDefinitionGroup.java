package dev.nokee.ide.visualstudio.internal.vcxproj;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Root(name = "ItemDefinitionGroup")
public class VCXItemDefinitionGroup {
	@Attribute
	@With String condition;

	@ElementListUnion({
		@ElementList(inline = true, type = VCXClCompile.Definition.class),
		@ElementList(inline = true, type = VCXLink.Definition.class)
	})
	List<? extends VCXItemDefinition> definitions;

	public static VCXItemDefinitionGroup empty() {
		return new VCXItemDefinitionGroup(null, ImmutableList.of());
	}

	public static VCXItemDefinitionGroup of(Iterable<? extends VCXItemDefinition> imports) {
		return new VCXItemDefinitionGroup(null, ImmutableList.copyOf(imports));
	}

	public static VCXItemDefinitionGroup of(VCXItemDefinition... imports) {
		return new VCXItemDefinitionGroup(null, ImmutableList.copyOf(imports));
	}
}
