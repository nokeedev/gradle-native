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

import com.google.common.collect.ImmutableList;
import lombok.Value;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@Root(name = "Filter")
public class VCXFilter implements VCXItem {
	@Attribute
	String include;

	@Element
	String uniqueIdentifier;

	@Element
	String extensions;

	public static final VCXFilter SOURCE_FILES = new VCXFilter("Source Files", "{4FC737F1-C7A5-4376-A066-2A32D752A2FF}", "cpp;c;cc;cxx;c++;def;odl;idl;hpj;bat;asm;asmx");
	public static final VCXFilter HEADER_FILES = new VCXFilter("Header Files", "{93995380-89BD-4b04-88EB-625FBE52EBFB}", "h;hh;hpp;hxx;h++;hm;inl;inc;ipp;xsd");
	public static final VCXFilter RESOURCE_FILES = new VCXFilter("Resource Files", "{67DA6AB6-F800-4c08-8B7A-83BB121AAD01}", "rc;ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe;resx;tiff;tif;png;wav;mfcribbon-ms");
	public static final List<VCXFilter> DEFAULT_FILTERS = ImmutableList.of(SOURCE_FILES, HEADER_FILES, RESOURCE_FILES);
}
