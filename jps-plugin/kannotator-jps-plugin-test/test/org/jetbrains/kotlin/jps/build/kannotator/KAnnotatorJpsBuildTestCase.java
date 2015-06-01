/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.jps.build.kannotator;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildResult;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.kotlin.jps.build.AbstractKotlinJpsBuildTestCase;
import org.jetbrains.kotlin.jps.build.classFilesComparison.ClassFilesComparisonPackage;

import java.io.File;

public class KAnnotatorJpsBuildTestCase extends AbstractKotlinJpsBuildTestCase {
    private static final String JDK_NAME = "1.6";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        File sourceFilesRoot = new File(TEST_DATA_PATH + File.separator + "kannotator");
        workDir = copyTestDataToTmpDir(sourceFilesRoot);
    }

    public void testMakeKannotator() {
        doTest();
    }

    private void doTest() {
        initProject();
        rebuildAll();
        for (JpsModule module : myProject.getModules()) {
            for (JpsModuleSourceRoot sourceRoot : module.getSourceRoots()) {
                processFile(sourceRoot.getFile());

                checkOutDirectoriesAfterMake(module, sourceRoot);
            }
        }
    }

    private void checkOutDirectoriesAfterMake(JpsModule module, JpsModuleSourceRoot sourceRoot) {
        File outDir = JpsJavaExtensionService.getInstance().getOutputDirectory(
                module, sourceRoot.getRootType() == JavaSourceRootType.TEST_SOURCE);
        File outDirAfterMake = new File(outDir.getPath() + "-after-make");
        rebuildAll();
        ClassFilesComparisonPackage.assertEqualDirectories(outDir, outDirAfterMake, false);
        FileUtil.delete(outDirAfterMake);
    }

    private void processFile(File root) {
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files == null) return;
            for (File file : files) {
                processFile(file);
            }
        }
        else if (root.getName().endsWith(".kt")) {
            System.out.println("Test started. File: " + root.getName());
            String path = root.getAbsolutePath();
            System.out.println("Change file: " + path);
            change(path);
            makeAll().assertSuccessful();
            System.out.println("Make successful,");
            System.out.println("Test successfully finished. File: " + root.getName());
            System.out.println("-----");
        }
    }

    @Override
    protected void rebuildAll() {
        System.out.println("'Rebuild all' started");
        super.rebuildAll();
        System.out.println("'Rebuild all' finished");
    }

    @NotNull
    @Override
    protected BuildResult makeAll() {
        System.out.println("'Make all' started");
        BuildResult result = super.makeAll();
        System.out.println("'Make all' finished");
        return result;
    }

    private void initProject() {
        addJdk(JDK_NAME);
        loadProject(workDir.getAbsolutePath());
        addKotlinRuntimeDependency();
    }

    @Override
    public void tearDown() throws Exception {
        FileUtil.delete(workDir);
        super.tearDown();
    }
}
