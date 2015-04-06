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

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass
import org.jetbrains.kotlin.load.kotlin.VirtualFileFinder
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClassFinder
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import java.util.EnumSet

public class CliVirtualFileFinder(private val packagesCache: JvmDependenciesIndex) : VirtualFileKotlinClassFinder(), VirtualFileFinder {

    override fun findVirtualFileWithHeader(classId: ClassId): VirtualFile? {
        val relativeClassName = classId.getRelativeClassName().asString().replace('.', '$')
        return packagesCache.findClass(classId, acceptedRootTypes = EnumSet.of(JavaRoot.RootType.BINARY)) { dir, _ ->
            dir.findChild("$relativeClassName.class")?.let {
                if (it.isValid()) it else null
            }
        }
    }

    override fun findKotlinClass(classId: ClassId): KotlinJvmBinaryClass? {
        val file = findVirtualFileWithHeader(classId) ?: return null
        return KotlinBinaryClassCache.getKotlinBinaryClass(file)
    }
}