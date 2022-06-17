/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.dockit.dsl.docbook.model;

import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import lombok.Value;

import java.util.*;

/**
 * Represents the documentation model for extensions contributed by a given plugin.
 */
@Value
public class ClassExtensionDoc {
	Set<ClassDoc> mixinClasses = new LinkedHashSet<>();
	Map<String, ClassDoc> extensionClasses = new LinkedHashMap<>();
	String pluginId;
	ClassMetaData targetClassMetaData;
	List<PropertyDoc> extraProperties = new ArrayList<>();
	List<BlockDoc> extraBlocks = new ArrayList<>();

	public ClassExtensionDoc(String pluginId, ClassMetaData targetClassMetaData) {
		this.pluginId = pluginId;
		this.targetClassMetaData = targetClassMetaData;
	}

	public List<PropertyDoc> getExtensionProperties() {
		List<PropertyDoc> properties = new ArrayList<>();
		mixinClasses.forEach(mixin -> {
			mixin.getClassProperties().forEach(prop -> {
				properties.add(prop.forClass(targetClassMetaData));
			});
		});
		extraProperties.forEach(prop -> {
			properties.add(prop.forClass(targetClassMetaData));
		});
		Collections.sort(properties, Comparator.comparing(PropertyDoc::getName));
		return properties;
	}

	public List<MethodDoc> getExtensionMethods() {
		List<MethodDoc> methods = new ArrayList<>();
		mixinClasses.forEach(mixin -> {
			mixin.getClassMethods().forEach(method -> {
				methods.add(method.forClass(targetClassMetaData));
			});
		});
		Collections.sort(methods, Comparator.comparing(it -> it.getMetaData().getOverrideSignature()));
		return methods;
	}

	public List<BlockDoc> getExtensionBlocks() {
		List<BlockDoc> blocks = new ArrayList<>();
		mixinClasses.forEach(mixin -> {
			mixin.getClassBlocks().forEach(block -> {
				blocks.add(block.forClass(targetClassMetaData));
			});
		});
		extraBlocks.forEach(block -> {
			blocks.add(block.forClass(targetClassMetaData));
		});
		Collections.sort(blocks, Comparator.comparing(BlockDoc::getName));
		return blocks;
	}
}

