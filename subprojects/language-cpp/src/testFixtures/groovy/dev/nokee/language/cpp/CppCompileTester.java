package dev.nokee.language.cpp;

import dev.nokee.language.base.testers.HasDestinationDirectoryTester;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.HasObjectFilesTester;
import dev.nokee.language.nativebase.NativeSourceCompileTester;

public interface CppCompileTester extends NativeSourceCompileTester, HasDestinationDirectoryTester, HasObjectFilesTester {
	CppCompile subject();
}
