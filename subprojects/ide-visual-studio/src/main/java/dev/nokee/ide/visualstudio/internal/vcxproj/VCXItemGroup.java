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
@Root(name = "ItemGroup")
public class VCXItemGroup {
	@Attribute(required = false)
	@With String label;

	@ElementListUnion({
		@ElementList(inline = true, type = VCXProjectConfiguration.class),
		@ElementList(inline = true, type = VCXClCompile.Item.class),
		@ElementList(inline = true, type = VCXClInclude.Item.class),
		@ElementList(inline = true, type = VCXNone.class),
		@ElementList(inline = true, type = VCXFilter.class)
	})
	List<? extends VCXItem> items;

	public static VCXItemGroup empty() {
		return new VCXItemGroup(null, ImmutableList.of());
	}

	public static VCXItemGroup of(Iterable<? extends VCXItem> items) {
		return new VCXItemGroup(null, ImmutableList.copyOf(items));
	}

	public static VCXItemGroup of(VCXItem... items) {
		return new VCXItemGroup(null, ImmutableList.copyOf(items));
	}
}
