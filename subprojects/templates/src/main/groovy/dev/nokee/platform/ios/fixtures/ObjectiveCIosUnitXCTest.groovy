package dev.nokee.platform.ios.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.nokee.platform.ios.fixtures.elements.NokeeAppUnitXCTestInfoPlist

class ObjectiveCIosUnitXCTest extends SourceElement {
	final SourceElement main
	final SourceElement unitTest = ofElements(new ObjectiveCUnitTest(), new NokeeAppUnitXCTestInfoPlist())

	ObjectiveCIosUnitXCTest(SourceElement main) {
		this.main = main
	}

	@Override
	List<SourceFile> getFiles() {
		return main.files + unitTest.files
	}

	@Override
	void writeToProject(File projectDir) {
		main.writeToProject(projectDir)
		unitTest.writeToProject(projectDir)
	}

	SourceElement withUiTest() {
		return new ObjectiveCIosUiXCTest(this);
	}

	private static class ObjectiveCUnitTest extends NativeSourceElement {
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
}
