package dev.nokee.language.objectivec;

import dev.nokee.language.base.testers.HasDestinationDirectoryTester;
import dev.nokee.language.nativebase.HasObjectFilesTester;
import dev.nokee.language.nativebase.NativeSourceCompileTester;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;

public interface ObjectiveCCompileTester extends NativeSourceCompileTester, HasDestinationDirectoryTester, HasObjectFilesTester {
	ObjectiveCCompile subject();
}
