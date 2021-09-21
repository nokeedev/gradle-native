/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
