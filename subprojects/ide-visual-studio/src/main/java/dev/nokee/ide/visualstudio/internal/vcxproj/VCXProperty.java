package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.Value;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

@Value(staticConstructor = "of")
public class VCXProperty {
	String key;
	String value;

	public static class Serializer implements Converter<VCXProperty> {
		public VCXProperty read(InputNode node) {
			throw new UnsupportedOperationException();
		}

		public void write(OutputNode node, VCXProperty external) {
			node.setName(external.getKey());
			node.setValue(external.getValue());
		}
	}
}
