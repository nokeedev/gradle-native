///*
// * Copyright 2022 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package dev.nokee.buildadapter.xcode.internal.plugins.specs;
//
//import com.google.common.collect.ImmutableMap;
//import dev.nokee.utils.Optionals;
//import dev.nokee.xcode.XCFileReference;
//import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
//import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
//import dev.nokee.xcode.objects.files.PBXFileReference;
//import dev.nokee.xcode.objects.files.PBXSourceTree;
//import dev.nokee.xcode.objects.targets.PBXTarget;
//import dev.nokee.xcode.project.Codeable;
//import dev.nokee.xcode.project.CodingKey;
//import dev.nokee.xcode.project.CodingKeyCoders;
//import dev.nokee.xcode.project.Encoder;
//import dev.nokee.xcode.project.KeyedCoder;
//import dev.nokee.xcode.project.KeyedEncoder;
//import dev.nokee.xcode.project.ValueCoder;
//import dev.nokee.xcode.project.ValueEncoder;
//import lombok.val;
//import org.gradle.api.tasks.Input;
//import org.gradle.api.tasks.InputFile;
//import org.gradle.api.tasks.Nested;
//import org.gradle.api.tasks.OutputDirectory;
//
//import javax.annotation.Nullable;
//import java.util.AbstractMap;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public final class DefaultXCBuildSpecFactory implements XCBuildSpecFactory {
//	private final CodingKeyCoders coders;
//
//	public DefaultXCBuildSpecFactory() {
//		this(new XCBuildSpecCodingKeyCoders());
//	}
//
//	public DefaultXCBuildSpecFactory(CodingKeyCoders coders) {
//		this.coders = coders;
//	}
//
//	@Override
//	public XCBuildSpec create(PBXTarget target) {
//		return encode(target).orElseThrow(RuntimeException::new);
//	}
//
//	private Optional<XCBuildSpec> encode(Object object) {
//		assert object instanceof Codeable;
//		return encode((Codeable) object);
//	}
//
//	public Optional<XCBuildSpec> encode(Codeable object) {
//		if (object instanceof PBXFileReference) { // Special handling... FIXME: We should handle this via coder
//			// pull reference full path (with build settings)
//			return Optional.of(new InputFileSpec((PBXFileReference) object)); // FIXME: this can be input OR outputs... maybe we should check specifically files on PBXBuildPhase
//		} else if (object instanceof PBXCopyFilesBuildPhase) {
//			XCBuildSpecEncodeContextAdapter context = new XCBuildSpecEncodeContextAdapter();
//			object.encode(context);
//			val r = (MapSpec) context.toSpec().orElseGet(() -> ofValue(Collections.emptyList()));
//			return Optional.of(ofValue(ImmutableMap.<String, XCBuildSpec>builder().putAll(r.values).put("destination", new OutputDirectorySpec(destinationReferenceOf((PBXCopyFilesBuildPhase) object))).build()));
//		} else if (object instanceof PBXShellScriptBuildPhase) {
//			// TODO: Track all arguments
//			// TODO: Track @InputFiles & @OutputFiles
//			throw new UnsupportedOperationException();
//		} else {
//			XCBuildSpecEncodeContextAdapter context = new XCBuildSpecEncodeContextAdapter();
//			object.encode(context);
//			return context.toSpec();
//		}
//	}
//
//	private <T> Optional<XCBuildSpec> encode(List<T> values, ValueCoder<T> coder) {
//		val result = values.stream().flatMap(it -> {
//			final XCBuildSpecEncoderAdapter encoder = new XCBuildSpecEncoderAdapter();
//			coder.encode(it, encoder);
//			return Optionals.stream(encoder.toSpec());
//		}).collect(Collectors.toList());
//
//		if (result.isEmpty()) {
//			return Optional.empty();
//		} else {
//			return Optional.of(ofValue(result));
//		}
//	}
//
//	private <T> Optional<XCBuildSpec> encode(T object, ValueCoder<T> coder) {
//		final XCBuildSpecEncoderAdapter encoder = new XCBuildSpecEncoderAdapter();
//		coder.encode(object, encoder);
//		return encoder.toSpec();
//	}
//
//	private class XCBuildSpecKeyedEncoderAdapter implements KeyedEncoder {
//		private final Map<String, XCBuildSpec> specs = new LinkedHashMap<>();
//
//		@Override
//		public <T> void encode(String key, T object, ValueCoder<T> coder) {
//			DefaultXCBuildSpecFactory.this.encode(object, coder).ifPresent(it -> specs.put(key, it));
//		}
//
//		@Override
//		public void encodeByRefObject(String key, Object object) {
//			throw new UnsupportedOperationException("This encoder doesn't support encoding object by reference");
////			DefaultXCBuildSpecFactory.this.encode(object).ifPresent(it -> specs.put(key, it));
//		}
//
//		@Override
//		public void encodeByCopyObject(String key, Object object) {
//			DefaultXCBuildSpecFactory.this.encode(object).ifPresent(it -> specs.put(key, it));
//		}
//
//		@Override
//		public void encodeString(String key, CharSequence string) {
//			specs.put(key, ofValue(string));
//		}
//
//		@Override
//		public void encodeInteger(String key, int integer) {
//			specs.put(key, ofValue(integer));
//		}
//
//		@Override
//		public void encodeDictionary(String key, Map<String, ?> dict) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public void encodeBoolean(String key, boolean value) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public <T> void encodeArray(String key, List<T> values, ValueCoder<T> coder) {
//			DefaultXCBuildSpecFactory.this.encode(values, coder).ifPresent(it -> specs.put(key, it));
//		}
//
//		public Optional<XCBuildSpec> toSpec() {
//			return Optional.of(ofValue(specs));
//		}
//	}
//
//	private final class XCBuildSpecEncoderAdapter implements Encoder {
//		@Nullable
//		private XCBuildSpec spec = null;
//
//		@Override
//		public void encodeByRefObject(Object object) {
//			spec = DefaultXCBuildSpecFactory.this.encode(object).orElse(null);
//		}
//
//		@Override
//		public void encodeByCopyObject(Object object) {
//			spec = DefaultXCBuildSpecFactory.this.encode(object).orElse(null);
//		}
//
//		@Override
//		public void encodeString(CharSequence string) {
//			spec = new InputSpec(string);
//		}
//
//		@Override
//		public void encodeInteger(int integer) {
//			spec = new InputSpec(integer);
//		}
//
//		@Override
//		public void encodeDictionary(Map<String, ?> dict) {
//			// TODO: encode dict
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public void encodeBoolean(boolean value) {
//			spec = new InputSpec(value);
//		}
//
//		@Override
//		public <T> void encodeArray(List<T> values, ValueEncoder<T, Object> encoder) {
//			spec = DefaultXCBuildSpecFactory.this.encode(values, encoder).orElse(null);
//		}
//
//		public Optional<XCBuildSpec> toSpec() {
//			return Optional.ofNullable(spec);
//		}
//	}
//
//	private final class XCBuildSpecEncodeContextAdapter implements Codeable.EncodeContext {
//		private final List<XCBuildSpec> specs = new ArrayList<>();
//
//		public Optional<XCBuildSpec> toSpec() {
//			if (specs.isEmpty()) {
//				return Optional.empty();
//			} else {
//				val builder = ImmutableMap.<String, XCBuildSpec>builder();
//				specs.stream().map(MapSpec.class::cast).forEach(it -> builder.putAll(it.values));
//				return Optional.of(ofValue(builder.build()));
//			}
//		}
//
//		@Override
//		public void base(Map<String, ?> fields) {
//
//		}
//
//		@Override
//		public void gid(String globalID) {
//
//		}
//
//		@Override
//		@SuppressWarnings("unchecked")
//		public void tryEncode(Map<CodingKey, ?> data) {
//			data.forEach((k, v) -> {
//				coders.get(k).map(it -> (KeyedCoder<Object>) it).ifPresent(coder -> {
//					val encoder = new XCBuildSpecKeyedEncoderAdapter();
//					coder.encode(v, encoder);
//					encoder.toSpec().ifPresent(specs::add);
//				});
//			});
//		}
//
//		@Override
//		public void noGid() {
//
//		}
//	}
//
//	interface LoadContext {
//		XCBuildSpec fileOf(PBXFileReference reference);
//	}
//
//	private static XCFileReference destinationReferenceOf(PBXCopyFilesBuildPhase buildPhase) {
//		switch (buildPhase.getDstSubfolderSpec()) {
//			case PlugIns: return XCFileReference.builtProduct("$(PLUGINS_FOLDER_PATH)", buildPhase.getDstPath());
//			case Wrapper: return XCFileReference.builtProduct("$(WRAPPER_NAME)", buildPhase.getDstPath());
//			case Resources: return XCFileReference.builtProduct("$(UNLOCALIZED_RESOURCES_FOLDER_PATH)", buildPhase.getDstPath());
//			case Frameworks: return XCFileReference.builtProduct("$(FRAMEWORKS_FOLDER_PATH)", buildPhase.getDstPath());
//			case Executables: return XCFileReference.builtProduct("$(EXECUTABLE_FOLDER_PATH)", buildPhase.getDstPath());
//			case JavaResources: return XCFileReference.builtProduct("$(JAVA_FOLDER_PATH)", buildPhase.getDstPath());
//			case SharedSupport: return XCFileReference.builtProduct("$(SHARED_SUPPORT_FOLDER_PATH)", buildPhase.getDstPath());
//			case SharedFrameworks: return XCFileReference.builtProduct("$(SHARED_FRAMEWORKS_FOLDER_PATH)", buildPhase.getDstPath());
//			case ProductsDirectory: return XCFileReference.builtProduct();
//			case AbsolutePath: return XCFileReference.absoluteFile(buildPhase.getDstPath());
//			default: throw new UnsupportedOperationException();
//		}
//	}
//
//	private static XCBuildSpec ofValue(CharSequence value) {
//		return new InputSpec(value);
//	}
//
//	private static XCBuildSpec ofValue(int integer) {
//		return new InputSpec(integer);
//	}
//
//	private static <E extends Enum<E>> XCBuildSpec ofValue(E value) {
//		return new InputSpec(value);
//	}
//
//	private static XCBuildSpec ofValue(List<XCBuildSpec> values) {
//		return new CompositeBuildSpec(values);
//	}
//
//	private static XCBuildSpec ofValue(Map<String, XCBuildSpec> values) {
//		return new MapSpec(values);
//	}
//
//	private static final class InputSpec implements XCBuildSpec {
//		private final Object value;
//
//		private InputSpec(Object value) {
//			this.value = value;
//		}
//
//		@Input
//		public Object getValue() {
//			return value;
//		}
//
//		@Override
//		public XCBuildSpec resolve(ResolveContext context) {
//			return this; // already resolved
//		}
//
//		@Override
//		public void visit(Visitor visitor) {
//			visitor.visitValue(value);
//		}
//	}
//
//	private static final class OutputDirectorySpec implements XCBuildSpec {
//		private final XCFileReference fileRef;
//
//		private OutputDirectorySpec(XCFileReference fileRef) {
//			this.fileRef = fileRef;
//		}
//
//		@OutputDirectory
//		public String getValue() {
//			return null; // force an error if Gradle actually resolve this spec
//			//fileRef.toString();
//		}
//
//		// TODO: Should be resolvable so we can snapshot the file itselfs
//
//		@Override
//		public XCBuildSpec resolve(ResolveContext context) {
//			throw new UnsupportedOperationException(); // FIXME: implement resolve
//		}
//
//		@Override
//		public void visit(Visitor visitor) {
//			visitor.visitValue(fileRef.toString());
//		}
//	}
//
//	private static final class MapSpec implements XCBuildSpec {
//		private final Map<String, XCBuildSpec> values;
//
//		private MapSpec(Map<String, XCBuildSpec> values) {
//			this.values = values;
//		}
//
//		@Nested
//		public Map<String, XCBuildSpec> getValues() {
//			return values;
//		}
//
//		@Override
//		public XCBuildSpec resolve(ResolveContext context) {
//			return new MapSpec(values.entrySet().stream().map(it -> new AbstractMap.SimpleImmutableEntry<>(it.getKey(), it.getValue().resolve(context))).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
//		}
//
//		@Override
//		public void visit(Visitor visitor) {
//			values.forEach((k, v) -> {
//				visitor.enterContext(k);
//				v.visit(visitor);
//				visitor.exitContext();
//			});
//		}
//	}
//
//	private static final class CompositeBuildSpec implements XCBuildSpec, Iterable<XCBuildSpec> {
//		private final List<XCBuildSpec> specs;
//
//		private CompositeBuildSpec(List<XCBuildSpec> specs) {
//			this.specs = specs;
//		}
//
//		@Override
//		public Iterator<XCBuildSpec> iterator() {
//			return specs.iterator();
//		}
//
//		@Override
//		public XCBuildSpec resolve(ResolveContext context) {
//			return new CompositeBuildSpec(specs.stream().map(it -> it.resolve(context)).collect(Collectors.toList()));
//		}
//
//		@Override
//		public void visit(Visitor visitor) {
//			for (int i = 0; i < specs.size(); i++) {
//				XCBuildSpec it = specs.get(i);
//				visitor.enterContext(String.valueOf(i));
//				it.visit(visitor);
//				visitor.exitContext();
//			}
//		}
//	}
//
//	private static final class InputFileSpec implements XCBuildSpec {
//		private final PBXFileReference reference;
//
//		private InputFileSpec(PBXFileReference reference) {
//			this.reference = reference;
//		}
//
//		@InputFile
//		public String getValue() {
//			return null; // force an error if Gradle actually resolve this spec
//			//fileRefs.get(reference).toString();
//		}
//
//		// TODO: Should be resolvable so we can snapshot the file itselfs
//
//		@Override
//		public XCBuildSpec resolve(ResolveContext context) {
//			throw new UnsupportedOperationException(); // FIXME: tresolve
//		}
//
//		@Override
//		public void visit(Visitor visitor) {
//			visitor.visitValue(toString(reference.getSourceTree()) + Optionals.or(reference.getPath(), reference::getName).map(it -> "/" + it).orElse(""));
//		}
//
//		private String toString(PBXSourceTree sourceTree) {
//			if (sourceTree.equals(PBXSourceTree.ABSOLUTE)) {
//				return "";
//			} else if (sourceTree.equals(PBXSourceTree.GROUP)) {
//				return sourceTree.toString();
//			} else {
//				return "$(" + sourceTree + ")";
//			}
//		}
//	}
//}
