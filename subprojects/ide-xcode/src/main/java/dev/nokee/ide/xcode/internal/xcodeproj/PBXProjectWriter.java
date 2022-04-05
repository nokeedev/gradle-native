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
package dev.nokee.ide.xcode.internal.xcodeproj;

import com.dd.plist.NSString;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import dev.nokee.xcode.AsciiPropertyListWriter;
import dev.nokee.xcode.PropertyListVersion;
import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PBXProjectWriter implements Closeable {
	private final GidGenerator gidGenerator;
	private final AsciiPropertyListWriter writer;

	public PBXProjectWriter(GidGenerator gidGenerator, Writer writer) {
		this.gidGenerator = gidGenerator;
		this.writer = new AsciiPropertyListWriter(writer, true);
	}

	public void write(PBXProject o) {
		writer.writeStartDocument(PropertyListVersion.VERSION_00);
		writer.writeStartDictionary(5);

		writer.writeDictionaryKey("archiveVersion");
		writer.writeInteger(1);

		writer.writeDictionaryKey("classes");
		writer.writeEmptyDictionary();

		writer.writeDictionaryKey("objectVersion");
		writer.writeInteger(46);


		writer.writeDictionaryKey("objects");
		val db = new Bob();
		val gid = objects(db, o);
		writer.writeStartDictionary(db.objects.size());
		for (Map.Entry<String, Obj> object : db.objects.entrySet()) {
			writer.writeDictionaryKey(object.getValue().id);
			writer.writeStartDictionary(object.getValue().fields.size());

			for (Map.Entry<String, Object> field : object.getValue().fields.entrySet()) {
				writer.writeDictionaryKey(field.getKey());
				write(writer, field.getValue());
			}

			writer.writeEndDictionary();
		}
		writer.writeEndDictionary();


		writer.writeDictionaryKey("rootObject");
		writer.writeString(gid);

		writer.writeEndDictionary();
		writer.writeEndDocument();

		writer.flush();
	}

	private void write(AsciiPropertyListWriter writer, Object value) {
		if (value instanceof Double) {
			writer.writeReal((Double) value);
		} else if (value instanceof Number) {
			writer.writeInteger(((Number) value).longValue());
		} else if (value instanceof String) {
			writer.writeString((String) value);
		} else if (value instanceof NSString) {
			writer.writeString(((NSString) value).getContent()); // because we have copy of NSDictionary with NSString
		} else if (value instanceof Boolean) {
			writer.writeBoolean((Boolean) value);
		} else if (value instanceof List) {
			if (((List<?>) value).isEmpty()) {
				writer.writeEmptyArray();
			} else {
				writer.writeStartArray(((List<?>) value).size());
				for (Object v : ((List<?>) value)) {
					write(writer, v);
				}
				writer.writeEndArray();
			}
		} else if (value instanceof Map) {
			if (((Map<?, ?>) value).isEmpty()) {
				writer.writeEmptyDictionary();
			} else {
				writer.writeStartDictionary(((Map<?, ?>) value).size());
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
					writer.writeDictionaryKey(entry.getKey().toString());
					write(writer, entry.getValue());
				}
				writer.writeEndDictionary();
			}
		} else {
			throw new RuntimeException(value.getClass().toString());
		}
	}

	private final Map<PBXObject, String> knownGlobalIds = new HashMap<>();

	private String objects(Bob db, PBXProject o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("mainGroup", objects(db, o.getMainGroup()));

			val targets = new ArrayList<>(o.getTargets());
			Collections.sort(targets, Ordering.natural().onResultOf(new Function<PBXTarget, String>() {
				@Override
				public String apply(PBXTarget input) {
					return input.getName();
				}
			}));
			obj.putField("targets", targets.stream().map(it -> objects(db, it)).collect(Collectors.toList()));
			obj.putField("buildConfigurationList", objects(db, o.getBuildConfigurationList()));
			obj.putField("compatibilityVersion", o.getCompatibilityVersion());
			obj.putField("attributes", ImmutableMap.of("LastUpgradeCheck", "0610"));
		}).id;
	}

	private String objects(Bob db, XCConfigurationList o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));
			val buildConfigurations = new ArrayList<>(o.getBuildConfigurationsByName().asMap().values());
			Collections.sort(buildConfigurations, new Comparator<XCBuildConfiguration>() {
				@Override
				public int compare(XCBuildConfiguration o1, XCBuildConfiguration o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			obj.putField("buildConfigurations", buildConfigurations.stream().map(it -> objects(db, it)).collect(Collectors.toList()));

			if (o.getDefaultConfigurationName().isPresent()) {
				obj.putField("defaultConfigurationName", o.getDefaultConfigurationName().get());
			}
			obj.putField("defaultConfigurationIsVisible", o.isDefaultConfigurationIsVisible() ? "YES" : "NO");
		}).id;
	}

	private String objects(Bob db, PBXReference o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("name", o.getName());
			if (o.getPath() != null) {
				obj.putField("path", o.getPath());
			}
			obj.putField("sourceTree", o.getSourceTree().toString());

			if (o instanceof PBXFileReference) {
				if (((PBXFileReference) o).getExplicitFileType().isPresent()) {
					obj.putField("explicitFileType", ((PBXFileReference) o).getExplicitFileType().get());
				}

				if (((PBXFileReference) o).getLastKnownFileType().isPresent()) {
					obj.putField("lastKnownFileType", ((PBXFileReference) o).getLastKnownFileType().get());
				}
			} else if (o instanceof PBXGroup) {
				List<PBXReference> children = new ArrayList<>(((PBXGroup) o).getChildren());
				if (((PBXGroup) o).getSortPolicy() == PBXGroup.SortPolicy.BY_NAME) {
					Collections.sort(children, new Comparator<PBXReference>() {
						@Override
						public int compare(PBXReference o1, PBXReference o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				}

				val childs = ImmutableList.builder();
				for (PBXReference child : children) {
					childs.add(objects(db, child));
				}
				obj.putField("children", childs.build());
			}
		}).id;
	}

	private String objects(Bob db, PBXTarget o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));

			obj.putField("name", o.getName());
			if (o.getProductType() != null) {
				obj.putField("productType", o.getProductType().toString());
			}
			if (o.getProductName() != null) {
				obj.putField("productName", o.getProductName());
			}
			if (o.getProductReference() != null) {
				obj.putField("productReference", objects(db, o.getProductReference()));
			}
			obj.putField("buildPhases", o.getBuildPhases().stream().map(it -> objects(db, it)).collect(Collectors.toList()));
			if (o.getBuildConfigurationList() != null) {
				obj.putField("buildConfigurationList", objects(db, o.getBuildConfigurationList()));
			}

			if (o instanceof PBXLegacyTarget) {
				obj.putField("buildArgumentsString", ((PBXLegacyTarget) o).getBuildArgumentsString());
				obj.putField("buildToolPath", ((PBXLegacyTarget) o).getBuildToolPath());
				if (((PBXLegacyTarget) o).getBuildWorkingDirectory() != null) {
					obj.putField("buildWorkingDirectory", ((PBXLegacyTarget) o).getBuildWorkingDirectory());
				}
				obj.putField("passBuildSettingsInEnvironment", ((PBXLegacyTarget) o).isPassBuildSettingsInEnvironment() ? "1" : "0");
			} else if (o instanceof PBXNativeTarget) {
				// nothing special
			}
		}).id;
	}

	private String objects(Bob db, PBXBuildStyle o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("name", o.getName());
			obj.putField("buildSettings", ImmutableMap.copyOf(o.getBuildSettings()));
		}).id;
	}

	private String objects(Bob db, PBXBuildPhase o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("files", o.getFiles().stream().map(it -> objects(db, it)).collect(Collectors.toList()));

			if (o instanceof PBXShellScriptBuildPhase) {
				obj.putField("inputPaths", ((PBXShellScriptBuildPhase) o).getInputPaths());
				obj.putField("outputPaths", ((PBXShellScriptBuildPhase) o).getOutputPaths());

				if (((PBXShellScriptBuildPhase) o).getShellPath() == null) {
					obj.putField("shellPath", "/bin/sh");
				} else {
					obj.putField("shellPath", ((PBXShellScriptBuildPhase) o).getShellPath());
				}

				if (((PBXShellScriptBuildPhase) o).getShellScript() == null) {
					obj.putField("shellScript", "");
				} else {
					obj.putField("shellScript", ((PBXShellScriptBuildPhase) o).getShellScript());
				}
			} else if (o instanceof PBXSourcesBuildPhase) {
				// nothing more to serialize
			}
		}).id;
	}

	private String objects(Bob db, PBXBuildFile o) {
		String gid = knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(o.isa(), o.stableHash()));
		return db.newObjectIfAbsent(gid, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("fileRef", objects(db, o.getFileRef()));
			if (o.getSettings().isPresent()) {
				obj.putField("settings", ImmutableMap.copyOf(o.getSettings().get()));
			}
		}).id;
	}

	private static String isa(PBXObject o) {
		return o.getClass().getSimpleName();
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	private static final class Bob {
		private final Map<String, Obj> objects = new LinkedHashMap<>();

		public Obj newObjectIfAbsent(String id, Consumer<? super Obj> action) {
			if (objects.containsKey(id)) {
				return objects.get(id);
			} else {
				val result = new Obj(id);
				action.accept(result);
				objects.put(id, result);
				return result;
			}
		}
	}

	private static final class Obj {
		private final String id;
		private final Map<String, Object> fields = new LinkedHashMap<>();

		private Obj(String id) {
			this.id = id;
		}

		public void putField(String name, Object value) {
			fields.put(name, value);
		}
	}
}
