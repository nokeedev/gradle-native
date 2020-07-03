package dev.nokee.ide.visualstudio.internal.vcxproj;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Root(name = "ImportGroup")
public class VCXImportGroup {
	@Attribute
	@With String label;

	@Attribute(required = false)
	@With String condition;

	@ElementList(inline = true, required = false)
	List<VCXImport> imports;

	public static VCXImportGroup empty(String label) {
		return new VCXImportGroup(label, null, ImmutableList.of());
	}

	public static VCXImportGroup of(Iterable<VCXImport> imports) {
		return new VCXImportGroup(null, null, ImmutableList.copyOf(imports));
	}

	public static VCXImportGroup of(VCXImport... imports) {
		return new VCXImportGroup(null, null, ImmutableList.copyOf(imports));
	}
}
