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

package org.jetbrains.kotlin.codegen.state

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.SoftHashMap
import org.jetbrains.kotlin.codegen.inline.InlineCodegenUtil
import org.jetbrains.kotlin.codegen.inline.InlineCodegenUtil.API
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.load.java.JvmAnnotationNames.KotlinSyntheticClass
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.commons.Method
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import java.util.HashMap

public class BytecodeCache {
    companion object {
        val KOTLIN_SYNTHETIC_CLASS_ANNOTATION_DESC = Type.getObjectType(KotlinSyntheticClass.CLASS_NAME.getInternalName()).getDescriptor()
    }

    data class MethodInfo(
            val firstLine: Int,
            val lastLine: Int,
            val node: MethodNode
    )

    data class ClassDebugInfo(
            val source: String?,
            val debug: String?
    ) {
        companion object {
            val EMPTY = ClassDebugInfo(null, null)
        }
    }

    data class CachedData(
            val debugInfo: ClassDebugInfo,
            val methods: Map<Method, MethodInfo>,
            val isPackagePart: Boolean
    )

    private val cache = SoftHashMap<VirtualFile, CachedData>()

    public fun loadCachedData(file: VirtualFile): CachedData {
        return cache.getOrPut(file) {
            processFile(file)
        }
    }

    private fun processFile(file: VirtualFile): CachedData {
        var debugInfo = ClassDebugInfo.EMPTY
        val methods = HashMap<Method, MethodInfo>()
        var isPackagePart = false

        ClassReader(file.contentsToByteArray()).accept(object : ClassVisitor(API) {
            override fun visitSource(source: String?, debug: String?) {
                debugInfo = ClassDebugInfo(source, debug)
            }

            override fun visitMethod(
                    access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?
            ): MethodVisitor? {
                return object : MethodNode(API, access, name, desc, signature, exceptions) {
                    var firstLine = Int.MAX_VALUE
                    var lastLine = Int.MIN_VALUE

                    override fun visitLineNumber(line: Int, start: Label) {
                        super.visitLineNumber(line, start)
                        firstLine = Math.min(firstLine, line)
                        lastLine = Math.max(lastLine, line)
                    }

                    override fun visitEnd() {
                        methods[Method(name, desc)] = MethodInfo(firstLine, lastLine, this)
                    }
                }
            }

            // We do this manually instead of via VirtualFileKotlinClass to avoid reading the class once again
            // TODO: factor out the necessary logic from VirtualFileKotlinClass and use it here
            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                if (desc != KOTLIN_SYNTHETIC_CLASS_ANNOTATION_DESC) return null

                return object : AnnotationVisitor(API) {
                    override fun visit(name: String?, value: Any) {
                        if (name == JvmAnnotationNames.KIND_FIELD_NAME) {
                            isPackagePart = value == KotlinSyntheticClass.Kind.PACKAGE_PART.name()
                        }
                    }
                }
            }
        }, ClassReader.SKIP_FRAMES or (if (InlineCodegenUtil.GENERATE_SMAP) 0 else ClassReader.SKIP_DEBUG))

        return CachedData(debugInfo, methods, isPackagePart)
    }
}
