package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCSourceElement

class ObjectiveCIosUnitXCTest extends SourceElement {
	final SourceElement main
	final SourceElement unitTest = ofElements(new ObjectiveCUnitTest(), new UnitXCTestInfoPlist())

	ObjectiveCIosUnitXCTest(SourceElement main) {
		this.main = main
	}

	@Override
	List<SourceFile> getFiles() {
		return main.files + unitTest.files
	}

	@Override
	void writeToProject(TestFile projectDir) {
		main.writeToProject(projectDir)
		unitTest.writeToProject(projectDir)
	}

	SourceElement withUiTest() {
		return new ObjectiveCIosUiXCTest(this);
	}
}

class UnitXCTestInfoPlist extends SourceFileElement {
	@Override
	SourceFile getSourceFile() {
		return sourceFile('resources', 'Info.plist', '''<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>CFBundleDevelopmentRegion</key>
	<string>$(DEVELOPMENT_LANGUAGE)</string>
	<key>CFBundleExecutable</key>
	<string>$(EXECUTABLE_NAME)</string>
	<key>CFBundleIdentifier</key>
	<string>$(PRODUCT_BUNDLE_IDENTIFIER)</string>
	<key>CFBundleInfoDictionaryVersion</key>
	<string>6.0</string>
	<key>CFBundleName</key>
	<string>$(PRODUCT_NAME)</string>
	<key>CFBundlePackageType</key>
	<string>$(PRODUCT_BUNDLE_PACKAGE_TYPE)</string>
	<key>CFBundleShortVersionString</key>
	<string>1.0</string>
	<key>CFBundleVersion</key>
	<string>1</string>
</dict>
</plist>
''')
	}

	@Override
	String getSourceSetName() {
		return 'unitTest'
	}
}

class ObjectiveCUnitTest extends ObjectiveCSourceElement {
	@Override
	SourceElement getHeaders() {
		return empty()
	}

	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('objc', 'objective_c_ios_applicationTests.m', '''
#import <XCTest/XCTest.h>

@interface objective_c_ios_applicationTests : XCTestCase

@end

@implementation objective_c_ios_applicationTests

- (void)setUp {
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
}

- (void)testExample {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
'''))
	}

	@Override
	String getSourceSetName() {
		return 'unitTest'
	}
}
