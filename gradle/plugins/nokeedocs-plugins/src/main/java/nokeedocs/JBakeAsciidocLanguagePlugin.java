/*
 * Copyright 2022 the original author or authors.
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
package nokeedocs;

import net.nokeedev.jbake.JBakeExtension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.MapProperty;

import java.util.stream.Collectors;

import static net.nokeedev.jbake.JBakeExtension.jbake;

class JBakeAsciidocLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().withPlugin("net.nokeedev.jbake-site", ignored -> {
			jbake(project, extension -> {
				final MapProperty<String, Object> asciidocAttributes = project.getObjects().mapProperty(String.class, Object.class);
				((ExtensionAware) extension).getExtensions().add("asciidocAttributes", asciidocAttributes);
				extension.getConfigurations().put("asciidoctor.attributes", asciidocAttributes.map(it ->
					it.entrySet().stream().map(t -> String.format("%s=%s", t.getKey(), t.getValue().toString())).collect(Collectors.joining(","))));
			});

			jbake(project, extension -> {
				asciidocAttributes(extension, attributes -> {
					attributes.put("jbake-version", project.getVersion());
					attributes.put("toc", "");
					attributes.put("toclevels", "1");
					attributes.put("toc-title", "Contents");
					attributes.put("icons", "font");
					attributes.put("idprefix", "");
					attributes.put("jbake-status", "published");
					attributes.put("encoding", "utf-8");
					attributes.put("lang", "en-US");
					attributes.put("sectanchors", "true");
					attributes.put("sectlinks", "true");
					attributes.put("linkattrs", "true");

					// TODO: We should add those attributes to each .adoc
					attributes.put("gradle-version", "current");
					attributes.put("gradle-user-manual", "https://docs.gradle.org/{gradle-version}/userguide");
					attributes.put("gradle-language-reference", "https://docs.gradle.org/{gradle-version}/dsl");
					attributes.put("gradle-api-reference", "https://docs.gradle.org/{gradle-version}/javadoc");
					attributes.put("gradle-guides", "https://guides.gradle.org/");
				});
			});
		});
	}

	private static void asciidocAttributes(JBakeExtension extension, Action<? super MapProperty<String, Object>> action) {
		action.execute((MapProperty<String, Object>) ((ExtensionAware) extension).getExtensions().getByName("asciidocAttributes"));
	}
}
