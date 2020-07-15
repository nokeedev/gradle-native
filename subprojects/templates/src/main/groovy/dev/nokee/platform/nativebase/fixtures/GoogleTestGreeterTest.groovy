package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement

class GoogleTestGreeterTest extends NativeSourceElement {

	@Override
	String getSourceSetName() {
		return 'test'
	}

	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('cpp', 'greeter_test.cpp', '''
			#include "gtest/gtest.h"

			#include "greeter.h"

			TEST(GreeterTest, can_greet_Alice) {
			  EXPECT_EQ(say_hello("Alice"), "Bonjour, Alice!");
			}
		'''))
	}
}
