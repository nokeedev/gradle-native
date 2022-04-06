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
package dev.nokee.xcode.project;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import dev.nokee.xcode.objects.PBXObject;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXSourcesBuildPhase;
import dev.nokee.xcode.objects.configuration.PBXBuildStyle;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PBXConverter {
	private final GidGenerator gidGenerator;

	public PBXConverter(GidGenerator gidGenerator) {
		this.gidGenerator = gidGenerator;
	}

	public PBXProj convert(PBXProject o) {
		PBXObjects.Builder objects = PBXObjects.builder();
		PBXObjectReference rootObject = objects(new ConvertContext(objects), o);
		return PBXProj.builder().objects(objects.build()).rootObject(rootObject.getGlobalID()).build();
	}

	private PBXObjectReference objects(ConvertContext db, PBXProject o) {
		return db.newObjectIfAbsent(o, obj -> {
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
		});
	}

	private PBXObjectReference objects(ConvertContext db, XCConfigurationList o) {
		return db.newObjectIfAbsent(o, obj -> {
			obj.putField("isa", isa(o));
			val buildConfigurations = new ArrayList<>(o.getBuildConfigurationsByName().values());
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
		});
	}

	private PBXObjectReference objects(ConvertContext db, PBXReference o) {
		return db.newObjectIfAbsent(o, obj -> {
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
		});
	}

	private PBXObjectReference objects(ConvertContext db, PBXTarget o) {
		return db.newObjectIfAbsent(o, obj -> {
			obj.putField("isa", isa(o));

			obj.putField("name", o.getName());
			if (o.getProductType() != null) {
				obj.putField("productType", o.getProductType());
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
		});
	}

	private PBXObjectReference objects(ConvertContext db, PBXBuildStyle o) {
		return db.newObjectIfAbsent(o, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("name", o.getName());
			obj.putField("buildSettings", o.getBuildSettings().asMap());
		});
	}

	private PBXObjectReference objects(ConvertContext db, PBXBuildPhase o) {
		return db.newObjectIfAbsent(o, obj -> {
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
		});
	}

	private PBXObjectReference objects(ConvertContext db, PBXBuildFile o) {
		return db.newObjectIfAbsent(o, obj -> {
			obj.putField("isa", isa(o));
			obj.putField("fileRef", objects(db, o.getFileRef()));
			if (!o.getSettings().isEmpty()) {
				obj.putField("settings", o.getSettings());
			}
		});
	}

	public static String isa(PBXObject o) {
		return o.getClass().getSimpleName();
	}

	private final class ConvertContext {
		private final PBXObjects.Builder builder;
		private final Map<PBXObject, String> knownGlobalIds = new HashMap<>();
		private final Map<String, PBXObjectReference> objects = new LinkedHashMap<>();

		private ConvertContext(PBXObjects.Builder builder) {
			this.builder = builder;
		}

		public PBXObjectReference newObjectIfAbsent(PBXObject o, Consumer<? super PBXObjectFields.Builder> action) {
			return newObjectIfAbsent(knownGlobalIds.computeIfAbsent(o, it -> gidGenerator.generateGid(isa(o), o.stableHash())), action);
		}

		public PBXObjectReference newObjectIfAbsent(String id, Consumer<? super PBXObjectFields.Builder> action) {
			if (objects.containsKey(id)) {
				return objects.get(id);
			} else {
				val result = PBXObjectReference.of(id, action);
				objects.put(id, result);
				builder.add(result);
				return result;
			}
		}
	}
}
