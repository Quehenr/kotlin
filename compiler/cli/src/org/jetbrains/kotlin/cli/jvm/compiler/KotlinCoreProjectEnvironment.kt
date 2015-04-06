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

package org.jetbrains.kotlin.cli.jvm.compiler

import com.intellij.core.CorePackageIndex
import com.intellij.core.JavaCoreApplicationEnvironment
import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.mock.MockFileIndexFacade
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import java.io.File

open class KotlinCoreProjectEnvironment(
        disposable: Disposable,
        applicationEnvironment: JavaCoreApplicationEnvironment
)
: JavaCoreProjectEnvironment(disposable, applicationEnvironment) {
    override fun createCoreFileManager() = CoreJavaFileManagerExt(PsiManager.getInstance(getProject()))

    public fun addRootToClasspath(file: VirtualFile) {
        val packageIndex = ServiceManager.getService(getProject(), javaClass<CorePackageIndex>())
        packageIndex.addToClasspath(file)

        val fileIndexFacade = ServiceManager.getService(getProject(), javaClass<MockFileIndexFacade>())
        fileIndexFacade.addLibraryRoot(file)

        val fileManager = ServiceManager.getService(getProject(), javaClass<CoreJavaFileManagerExt>())
        fileManager.addToClasspath(file)
    }

    public fun addJavaSourceRoot(file: VirtualFile) {
        addRootToClasspath(file)
    }
}