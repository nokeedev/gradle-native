package dev.nokeebuild.licensing;

import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

abstract /*final*/ class NokeeLicenseExtension implements LicenseExtension {
	@Inject
	public NokeeLicenseExtension() throws MalformedURLException {
		getDisplayName().value("The Apache Software License, Version 2.0").disallowChanges();
		getLicenseUrl().value(new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")).disallowChanges();
		getName().value("Apache-2.0").disallowChanges();
		getShortName().value("ASL2").disallowChanges();
		getCopyrightFileHeader().value("Copyright ${today.year} the original author or authors.\n"
			+ "\n"
			+ "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
			+ "you may not use this file except in compliance with the License.\n"
			+ "You may obtain a copy of the License at\n"
			+ "\n"
			+ "    https://www.apache.org/licenses/LICENSE-2.0\n"
			+ "\n"
			+ "Unless required by applicable law or agreed to in writing, software\n"
			+ "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
			+ "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
			+ "See the License for the specific language governing permissions and\n"
			+ "limitations under the License.").disallowChanges();
	}

	@Override
	public abstract Property<String> getDisplayName();

	@Override
	public abstract Property<URL> getLicenseUrl();

	@Override
	public abstract Property<String> getName();

	@Override
	public abstract Property<String> getShortName();

	@Override
	public abstract Property<String> getCopyrightFileHeader();
}
