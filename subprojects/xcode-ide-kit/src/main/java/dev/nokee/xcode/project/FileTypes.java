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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * File types used in Apple targets.
 */
public final class FileTypes {
	private FileTypes() {}

	/**
	 * Map of file extension to Xcode identifiers.
	 */
	public static final ImmutableMap<String, String> FILE_EXTENSION_TO_IDENTIFIER =
		ImmutableMap.<String, String>builder()
			  .put("1", "text.man")
			  .put("a", "archive.ar")
			  .put("ada", "sourcecode.ada")
			  .put("adb", "sourcecode.ada")
			  .put("ads", "sourcecode.ada")
			  .put("aiff", "audio.aiff")
			  .put("app", "wrapper.application")
			  .put("appex", "wrapper.app-extension")
			  .put("applescript", "sourcecode.applescript")
			  .put("archivingdescription", "text.xml.ibArchivingDescription")
			  .put("asdictionary", "archive.asdictionary")
			  .put("asm", "sourcecode.asm.asm")
			  .put("au", "audio.au")
			  .put("avi", "video.avi")
			  .put("bdic", "file")
			  .put("bin", "archive.macbinary")
			  .put("bmp", "image.bmp")
			  .put("bundle", "wrapper.cfbundle")
			  .put("c", "sourcecode.c.c")
			  .put("c++", "sourcecode.cpp.cpp")
			  .put("cc", "sourcecode.cpp.cpp")
			  .put("cdda", "audio.aiff")
			  .put("cl", "sourcecode.opencl")
			  .put("class", "compiled.javaclass")
			  .put("classdescription", "text.plist.ibClassDescription")
			  .put("classdescriptions", "text.plist.ibClassDescription")
			  .put("codingdescription", "text.xml.ibCodingDescription")
			  .put("cp", "sourcecode.cpp.cpp")
			  .put("cpp", "sourcecode.cpp.cpp")
			  .put("csh", "text.script.csh")
			  .put("css", "text.css")
			  .put("ctrl", "sourcecode.glsl")
			  .put("cxx", "sourcecode.cpp.cpp")
			  .put("d", "sourcecode.dtrace")
			  .put("dart", "sourcecode")
			  .put("data", "compiled")
			  .put("defs", "sourcecode.mig")
			  .put("dict", "text.plist")
			  .put("dsym", "wrapper.dsym")
			  .put("dylan", "sourcecode.dylan")
			  .put("dylib", "compiled.mach-o.dylib")
			  .put("ear", "archive.ear")
			  .put("eval", "sourcecode.glsl")
			  .put("exp", "sourcecode.exports")
			  .put("f", "sourcecode.fortran")
			  .put("f77", "sourcecode.fortran.f77")
			  .put("f90", "sourcecode.fortran.f90")
			  .put("f95", "sourcecode.fortran.f90")
			  .put("for", "sourcecode.fortran")
			  .put("frag", "sourcecode.glsl")
			  .put("fragment", "sourcecode.glsl")
			  .put("framework", "wrapper.framework")
			  .put("fs", "sourcecode.glsl")
			  .put("fsh", "sourcecode.glsl")
			  .put("geom", "sourcecode.glsl")
			  .put("geometry", "sourcecode.glsl")
			  .put("gif", "image.gif")
			  .put("gmk", "sourcecode.make")
			  .put("gs", "sourcecode.glsl")
			  .put("gsh", "sourcecode.glsl")
			  .put("gyp", "sourcecode")
			  .put("gypi", "text")
			  .put("gz", "archive.gzip")
			  .put("h", "sourcecode.c.h")
			  .put("h++", "sourcecode.cpp.h")
			  .put("hh", "sourcecode.cpp.h")
			  .put("hp", "sourcecode.cpp.h")
			  .put("hpp", "sourcecode.cpp.h")
			  .put("hqx", "archive.binhex")
			  .put("htm", "text.html")
			  .put("html", "text.html")
			  .put("htmld", "wrapper.htmld")
			  .put("hxx", "sourcecode.cpp.h")
			  .put("i", "sourcecode.c.c.preprocessed")
			  .put("icns", "image.icns")
			  .put("ico", "image.ico")
			  .put("iconset", "folder.iconset")
			  .put("ii", "sourcecode.cpp.cpp.preprocessed")
			  .put("imagecatalog", "folder.imagecatalog")
			  .put("inc", "sourcecode.pascal")
			  .put("inl", "sourcecode.cpp.h")
			  .put("ipp", "sourcecode.cpp.h")
			  .put("jam", "sourcecode.jam")
			  .put("jar", "archive.jar")
			  .put("java", "sourcecode.java")
			  .put("javascript", "sourcecode.javascript")
			  .put("jpeg", "image.jpeg")
			  .put("jpg", "image.jpeg")
			  .put("js", "sourcecode.javascript")
			  .put("jscript", "sourcecode.javascript")
			  .put("json", "text.json")
			  .put("jsp", "text.html.other")
			  .put("kext", "wrapper.kernel-extension")
			  .put("l", "sourcecode.lex")
			  .put("lid", "sourcecode.dylan")
			  .put("ll", "sourcecode.asm.llvm")
			  .put("llx", "sourcecode.asm.llvm")
			  .put("lm", "sourcecode.lex")
			  .put("lmm", "sourcecode.lex")
			  .put("lp", "sourcecode.lex")
			  .put("lpp", "sourcecode.lex")
			  .put("lxx", "sourcecode.lex")
			  .put("m", "sourcecode.c.objc")
			  .put("mak", "sourcecode.make")
			  .put("map", "sourcecode.module-map")
			  .put("markdown", "net.daringfireball.markdown")
			  .put("md", "net.daringfireball.markdown")
			  .put("mdimporter", "wrapper.spotlight-importer")
			  .put("mdown", "net.daringfireball.markdown")
			  .put("mi", "sourcecode.c.objc.preprocessed")
			  .put("mid", "audio.midi")
			  .put("midi", "audio.midi")
			  .put("mig", "sourcecode.mig")
			  .put("mii", "sourcecode.cpp.objcpp.preprocessed")
			  .put("mm", "sourcecode.cpp.objcpp")
			  .put("modulemap", "sourcecode.module-map")
			  .put("moov", "video.quicktime")
			  .put("mov", "video.quicktime")
			  .put("mp3", "audio.mp3")
			  .put("mpeg", "video.mpeg")
			  .put("mpg", "video.mpeg")
			  .put("mpkg", "wrapper.installer-mpkg")
			  .put("nasm", "sourcecode.nasm")
			  .put("nib", "wrapper.nib")
			  .put("nib~", "wrapper.nib")
			  .put("nqc", "sourcecode.nqc")
			  .put("o", "compiled.mach-o.objfile")
			  .put("octest", "wrapper.cfbundle")
			  .put("p", "sourcecode.pascal")
			  .put("pas", "sourcecode.pascal")
			  .put("pbfilespec", "text.plist.pbfilespec")
			  .put("pblangspec", "text.plist.pblangspec")
			  .put("pbxproj", "text.pbxproject")
			  .put("pch", "sourcecode.c.h")
			  .put("pch++", "sourcecode.cpp.h")
			  .put("pct", "image.pict")
			  .put("pdf", "image.pdf")
			  .put("perl", "text.script.perl")
			  .put("php", "text.script.php")
			  .put("php3", "text.script.php")
			  .put("php4", "text.script.php")
			  .put("phtml", "text.script.php")
			  .put("pict", "image.pict")
			  .put("pkg", "wrapper.installer-pkg")
			  .put("pl", "text.script.perl")
			  .put("plist", "text.plist")
			  .put("pluginkit", "wrapper.app-extension")
			  .put("pm", "text.script.perl")
			  .put("png", "image.png")
			  .put("pp", "sourcecode.pascal")
			  .put("ppob", "archive.ppob")
			  .put("prefpane", "wrapper.cfbundle")
			  .put("proto", "text")
			  .put("py", "text.script.python")
			  .put("qtz", "video.quartz-composer")
			  .put("r", "sourcecode.rez")
			  .put("rb", "text.script.ruby")
			  .put("rbw", "text.script.ruby")
			  .put("rcx", "compiled.rcx")
			  .put("rez", "sourcecode.rez")
			  .put("rhtml", "text.html.other")
			  .put("rsrc", "archive.rsrc")
			  .put("rtf", "text.rtf")
			  .put("rtfd", "wrapper.rtfd")
			  .put("s", "sourcecode.asm")
			  .put("scnassets", "wrapper.scnassets")
			  .put("scriptSuite", "text.plist.scriptSuite")
			  .put("scriptTerminology", "text.plist.scriptTerminology")
			  .put("sh", "text.script.sh")
			  .put("shtml", "text.html.other")
			  .put("sit", "archive.stuffit")
			  .put("storyboard", "file.storyboard")
			  .put("strings", "text.plist.strings")
			  .put("stringsdict", "file.bplist")
			  .put("tar", "archive.tar")
			  .put("tbd", "sourcecode.text-based-dylib-definition")
			  .put("tcc", "sourcecode.cpp.cpp")
			  .put("text", "net.daringfireball.markdown")
			  .put("tif", "image.tiff")
			  .put("tiff", "image.tiff")
			  .put("ttf", "file")
			  .put("txt", "text")
			  .put("uicatalog", "file.uicatalog")
			  .put("vert", "sourcecode.glsl")
			  .put("vertex", "sourcecode.glsl")
			  .put("view", "archive.rsrc")
			  .put("vs", "sourcecode.glsl")
			  .put("vsh", "sourcecode.glsl")
			  .put("war", "archive.war")
			  .put("wav", "audio.wav")
			  .put("worksheet", "text.script.worksheet")
			  .put("xcassets", "folder.assetcatalog")
			  .put("xcbuildrules", "text.plist.xcbuildrules")
			  .put("xcconfig", "text.xcconfig")
			  .put("xcdatamodel", "wrapper.xcdatamodel")
			  .put("xcdatamodeld", "wrapper.xcdatamodeld")
			  .put("xclangspec", "text.plist.xclangspec")
			  .put("xcode", "wrapper.pb-project")
			  .put("xcodeproj", "wrapper.pb-project")
			  .put("xcspec", "text.plist.xcspec")
			  .put("xcsynspec", "text.plist.xcsynspec")
			  .put("xctarget", "wrapper.pb-target")
			  .put("xctest", "wrapper.cfbundle")
			  .put("xctxtmacro", "text.plist.xctxtmacro")
			  .put("xcworkspace", "wrapper.workspace")
			  .put("xib", "file.xib")
			  .put("xpc", "wrapper.xpc-service")
			  .put("y", "sourcecode.yacc")
			  .put("ym", "sourcecode.yacc")
			  .put("ymm", "sourcecode.yacc")
			  .put("yp", "sourcecode.yacc")
			  .put("ypp", "sourcecode.yacc")
			  .put("yxx", "sourcecode.yacc")
			  .put("zip", "archive.zip")
			  .build();

	/**
	 * Set of identifiers which only work as "lastKnownFileType" and not "explicitFileType" in a PBXFileReference.
	 *
	 * <p>Yes, really. Because Xcode.</p>
	 *
	 * <p>I can't agree, more...</p>
	 */
	public static final ImmutableSet<String> EXPLICIT_FILE_TYPE_BROKEN_IDENTIFIERS =
		ImmutableSet.of("file.xib", "file.storyboard", "wrapper.scnassets");

	/**
	 * Set of identifiers for which we will use "lastKnownFileType" instead of "explicitFileType" in a
	 * PBXFileReference to allow the user to change the type by renaming the file.
	 */
	public static final ImmutableSet<String> MODIFIABLE_FILE_TYPE_IDENTIFIERS =
		ImmutableSet.of(
			"sourcecode.c.c",
			"sourcecode.c.h",
			"sourcecode.cpp.cpp",
			"sourcecode.cpp.h",
			"sourcecode.c.objc",
			"sourcecode.cpp.objcpp");


//    /**
//     * Apple UTI for executables.
//     */
//    MACH_O_EXECUTABLE("", "compiled.mach-o.executable"),
//
//    /**
//     * Apple UTI for dynamic libraries.
//     */
//    MACH_O_DYNAMIC_LIBRARY("dylib", "compiled.mach-o.dylib"),
//
//    /**
//     * Apple UTI for static libraries.
//     */
//    ARCHIVE_LIBRARY("a", "archive.ar"),
//
//    C_SOURCE_CODE("c", "sourcecode.c.c"),
//    CC_SOURCE_CODE("cc", "sourcecode.cpp.cpp"),
//    CPP_SOURCE_CODE("cpp", "sourcecode.cpp.cpp"),
//    CXX_SOURCE_CODE("cxx", "sourcecode.cpp.cpp"),
//    H_SOURCE_CODE("h", "sourcecode.c.h"),
//    SWIFT_SOURCE_CODE("swift", "sourcecode.swift"),
//    XCODE_PROJECT_WRAPPER("xcodeproj", "wrapper.pb-project");
//
//    public final String fileExtension;
//    public final String identifier;
//    FileTypes(String fileExtension, String identifier) {
//        this.fileExtension = fileExtension;
//        this.identifier = identifier;
//    }
//
//
//    /**
//     * Map of file extension to Apple UTI (Uniform Type Identifier).
//     */
//    public static final ImmutableMap<String, String> FILE_EXTENSION_TO_UTI;
//
//    static {
//        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
//        for (org.gradle.ide.xcode.internal.xcodeproj.FileTypes fileType : org.gradle.ide.xcode.internal.xcodeproj.FileTypes.values()) {
//            builder.put(fileType.fileExtension, fileType.identifier);
//        }
//
//        FILE_EXTENSION_TO_UTI = builder.build();
//    }
//
//    /**
//     * Multimap of Apple UTI (Uniform Type Identifier) to file extension(s).
//     */
//    public static final ImmutableMultimap<String, String> UTI_TO_FILE_EXTENSIONS;
//
//    static {
//        // Invert the map of (file extension -> UTI) pairs to
//        // (UTI -> [file extension 1, ...]) pairs.
//        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
//        for (ImmutableMap.Entry<String, String> entry : FILE_EXTENSION_TO_UTI.entrySet()) {
//            builder.put(entry.getValue(), entry.getKey());
//        }
//        UTI_TO_FILE_EXTENSIONS = builder.build();
//    }
}
