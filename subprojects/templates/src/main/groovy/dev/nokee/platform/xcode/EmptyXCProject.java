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
package dev.nokee.platform.xcode;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EmptyXCProject extends SourceElement {
	private final String projectName;

	public EmptyXCProject(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public List<SourceFile> getFiles() {
		return Arrays.asList(
			sourceFile(projectName + ".xcodeproj", "project.pbxproj", Stream.of(
				"// !$*UTF8*$!",
				"{",
				"\tarchiveVersion = 1;",
				"\tclasses = {",
				"\t};",
				"\tobjectVersion = 55;",
				"\tobjects = {",
				"",
				"/* Begin PBXGroup section */",
				"\t\t6552A5BA27F5F71D00D9A851 = {",
				"\t\t\tisa = PBXGroup;",
				"\t\t\tchildren = (",
				"\t\t\t\t6552A5C827F5F71F00D9A851 /* Products */,",
				"\t\t\t);",
				"\t\t\tsourceTree = \"<group>\";",
				"\t\t};",
				"\t\t6552A5C827F5F71F00D9A851 /* Products */ = {",
				"\t\t\tisa = PBXGroup;",
				"\t\t\tchildren = (",
				"\t\t\t);",
				"\t\t\tname = Products;",
				"\t\t\tsourceTree = \"<group>\";",
				"\t\t};",
				"/* End PBXGroup section */",
				"",
				"/* Begin PBXProject section */",
				"\t\t6552A5BB27F5F71D00D9A851 /* Project object */ = {",
				"\t\t\tisa = PBXProject;",
				"\t\t\tattributes = {",
				"\t\t\t\tBuildIndependentTargetsInParallel = 1;",
				"\t\t\t\tLastSwiftUpdateCheck = 1320;",
				"\t\t\t\tLastUpgradeCheck = 1320;",
				"\t\t\t};",
				"\t\t\tbuildConfigurationList = 6552A5BE27F5F71D00D9A851 /* Build configuration list for PBXProject \"p, */;\n" +
				"\t\t\tcompatibilityVersion = \"Xcode 13.0\";",
				"\t\t\tdevelopmentRegion = en;",
				"\t\t\thasScannedForEncodings = 0;",
				"\t\t\tknownRegions = (",
				"\t\t\t\ten,",
				"\t\t\t\tBase,",
				"\t\t\t);",
				"\t\t\tmainGroup = 6552A5BA27F5F71D00D9A851;",
				"\t\t\tproductRefGroup = 6552A5C827F5F71F00D9A851 /* Products */;",
				"\t\t\tprojectDirPath = \"\";",
				"\t\t\tprojectRoot = \"\";",
				"\t\t\ttargets = (",
				"\t\t\t);",
				"\t\t};",
				"/* End PBXProject section */",
				"",
				"/* Begin XCBuildConfiguration section */",
				"\t\t6552A5EE27F5F71F00D9A851 /* Debug */ = {",
				"\t\t\tisa = XCBuildConfiguration;",
				"\t\t\tbuildSettings = {",
				"\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;",
				"\t\t\t\tCLANG_ANALYZER_NONNULL = YES;",
				"\t\t\t\tCLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;",
				"\t\t\t\tCLANG_CXX_LANGUAGE_STANDARD = \"gnu++17\";",
				"\t\t\t\tCLANG_CXX_LIBRARY = \"libc++\";",
				"\t\t\t\tCLANG_ENABLE_MODULES = YES;",
				"\t\t\t\tCLANG_ENABLE_OBJC_ARC = YES;",
				"\t\t\t\tCLANG_ENABLE_OBJC_WEAK = YES;",
				"\t\t\t\tCLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;",
				"\t\t\t\tCLANG_WARN_BOOL_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_COMMA = YES;",
				"\t\t\t\tCLANG_WARN_CONSTANT_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;",
				"\t\t\t\tCLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;",
				"\t\t\t\tCLANG_WARN_DOCUMENTATION_COMMENTS = YES;",
				"\t\t\t\tCLANG_WARN_EMPTY_BODY = YES;",
				"\t\t\t\tCLANG_WARN_ENUM_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_INFINITE_RECURSION = YES;",
				"\t\t\t\tCLANG_WARN_INT_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;",
				"\t\t\t\tCLANG_WARN_OBJC_LITERAL_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;",
				"\t\t\t\tCLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;",
				"\t\t\t\tCLANG_WARN_RANGE_LOOP_ANALYSIS = YES;",
				"\t\t\t\tCLANG_WARN_STRICT_PROTOTYPES = YES;",
				"\t\t\t\tCLANG_WARN_SUSPICIOUS_MOVE = YES;",
				"\t\t\t\tCLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;",
				"\t\t\t\tCLANG_WARN_UNREACHABLE_CODE = YES;",
				"\t\t\t\tCLANG_WARN__DUPLICATE_METHOD_MATCH = YES;",
				"\t\t\t\tCOPY_PHASE_STRIP = NO;",
				"\t\t\t\tDEBUG_INFORMATION_FORMAT = dwarf;",
				"\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;",
				"\t\t\t\tENABLE_TESTABILITY = YES;",
				"\t\t\t\tGCC_C_LANGUAGE_STANDARD = gnu11;",
				"\t\t\t\tGCC_DYNAMIC_NO_PIC = NO;",
				"\t\t\t\tGCC_NO_COMMON_BLOCKS = YES;",
				"\t\t\t\tGCC_OPTIMIZATION_LEVEL = 0;",
				"\t\t\t\tGCC_PREPROCESSOR_DEFINITIONS = (",
				"\t\t\t\t\t\"DEBUG=1\",",
				"\t\t\t\t\t\"$(inherited)\",",
				"\t\t\t\t);",
				"\t\t\t\tGCC_WARN_64_TO_32_BIT_CONVERSION = YES;",
				"\t\t\t\tGCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;",
				"\t\t\t\tGCC_WARN_UNDECLARED_SELECTOR = YES;",
				"\t\t\t\tGCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;",
				"\t\t\t\tGCC_WARN_UNUSED_FUNCTION = YES;",
				"\t\t\t\tGCC_WARN_UNUSED_VARIABLE = YES;",
				"\t\t\t\tMTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;",
				"\t\t\t\tMTL_FAST_MATH = YES;",
				"\t\t\t\tONLY_ACTIVE_ARCH = YES;",
				"\t\t\t\tSWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG;",
				"\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = \"-Onone\";",
				"\t\t\t};",
				"\t\t\tname = Debug;",
				"\t\t};",
				"\t\t6552A5EF27F5F71F00D9A851 /* Release */ = {",
				"\t\t\tisa = XCBuildConfiguration;",
				"\t\t\tbuildSettings = {",
				"\t\t\t\tALWAYS_SEARCH_USER_PATHS = NO;",
				"\t\t\t\tCLANG_ANALYZER_NONNULL = YES;",
				"\t\t\t\tCLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;",
				"\t\t\t\tCLANG_CXX_LANGUAGE_STANDARD = \"gnu++17\";",
				"\t\t\t\tCLANG_CXX_LIBRARY = \"libc++\";",
				"\t\t\t\tCLANG_ENABLE_MODULES = YES;",
				"\t\t\t\tCLANG_ENABLE_OBJC_ARC = YES;",
				"\t\t\t\tCLANG_ENABLE_OBJC_WEAK = YES;",
				"\t\t\t\tCLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;",
				"\t\t\t\tCLANG_WARN_BOOL_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_COMMA = YES;",
				"\t\t\t\tCLANG_WARN_CONSTANT_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;",
				"\t\t\t\tCLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;",
				"\t\t\t\tCLANG_WARN_DOCUMENTATION_COMMENTS = YES;",
				"\t\t\t\tCLANG_WARN_EMPTY_BODY = YES;",
				"\t\t\t\tCLANG_WARN_ENUM_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_INFINITE_RECURSION = YES;",
				"\t\t\t\tCLANG_WARN_INT_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;",
				"\t\t\t\tCLANG_WARN_OBJC_LITERAL_CONVERSION = YES;",
				"\t\t\t\tCLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;",
				"\t\t\t\tCLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;",
				"\t\t\t\tCLANG_WARN_RANGE_LOOP_ANALYSIS = YES;",
				"\t\t\t\tCLANG_WARN_STRICT_PROTOTYPES = YES;",
				"\t\t\t\tCLANG_WARN_SUSPICIOUS_MOVE = YES;",
				"\t\t\t\tCLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;",
				"\t\t\t\tCLANG_WARN_UNREACHABLE_CODE = YES;",
				"\t\t\t\tCLANG_WARN__DUPLICATE_METHOD_MATCH = YES;",
				"\t\t\t\tCOPY_PHASE_STRIP = NO;",
				"\t\t\t\tDEBUG_INFORMATION_FORMAT = \"dwarf-with-dsym\";",
				"\t\t\t\tENABLE_NS_ASSERTIONS = NO;",
				"\t\t\t\tENABLE_STRICT_OBJC_MSGSEND = YES;",
				"\t\t\t\tGCC_C_LANGUAGE_STANDARD = gnu11;",
				"\t\t\t\tGCC_NO_COMMON_BLOCKS = YES;",
				"\t\t\t\tGCC_WARN_64_TO_32_BIT_CONVERSION = YES;",
				"\t\t\t\tGCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;",
				"\t\t\t\tGCC_WARN_UNDECLARED_SELECTOR = YES;",
				"\t\t\t\tGCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;",
				"\t\t\t\tGCC_WARN_UNUSED_FUNCTION = YES;",
				"\t\t\t\tGCC_WARN_UNUSED_VARIABLE = YES;",
				"\t\t\t\tMTL_ENABLE_DEBUG_INFO = NO;",
				"\t\t\t\tMTL_FAST_MATH = YES;",
				"\t\t\t\tSWIFT_COMPILATION_MODE = wholemodule;",
				"\t\t\t\tSWIFT_OPTIMIZATION_LEVEL = \"-O\";",
				"\t\t\t};",
				"\t\t\tname = Release;",
				"\t\t};",
				"/* End XCBuildConfiguration section */",
				"",
				"/* Begin XCConfigurationList section */",
				"\t\t6552A5BE27F5F71D00D9A851 /* Build configuration list for PBXProject \"App\" */ = {",
				"\t\t\tisa = XCConfigurationList;",
				"\t\t\tbuildConfigurations = (",
				"\t\t\t\t6552A5EE27F5F71F00D9A851 /* Debug */,",
				"\t\t\t\t6552A5EF27F5F71F00D9A851 /* Release */,",
				"\t\t\t);",
				"\t\t\tdefaultConfigurationIsVisible = 0;",
				"\t\t\tdefaultConfigurationName = Release;",
				"\t\t};",
				"/* End XCConfigurationList section */",
				"\t};",
				"\trootObject = 6552A5BB27F5F71D00D9A851 /* Project object */;",
				"}").collect(Collectors.joining(System.lineSeparator()))),
			sourceFile(projectName + ".xcodeproj/project.xcworkspace", "contents.xcworkspacedata", Stream.of(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
				"<Workspace",
				"   version = \"1.0\">",
				"   <FileRef",
				"      location = \"self:\">",
				"   </FileRef>",
				"</Workspace>").collect(Collectors.joining(System.lineSeparator())))
		);
	}

	@Override
	public void writeToProject(File projectDir) {
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToDirectory(projectDir);
		}
	}

	public void writeToProject(Path projectDirectory) {
		writeToProject(projectDirectory.toFile());
	}
}
