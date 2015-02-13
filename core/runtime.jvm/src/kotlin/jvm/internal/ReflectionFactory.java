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

package kotlin.jvm.internal;

import kotlin.jvm.KotlinReflectionNotSupportedError;
import kotlin.reflect.*;

public class ReflectionFactory {
    public KClass createKotlinClass(Class javaClass) {
        return null;
    }

    public KPackage createKotlinPackage(Class javaClass) {
        return null;
    }

    public KClass foreignKotlinClass(Class javaClass) {
        throw error();
    }

    public KMemberProperty memberProperty(String name, KClass owner) {
        throw error();
    }

    public KMutableMemberProperty mutableMemberProperty(String name, KClass owner) {
        throw error();
    }

    public KTopLevelVariable topLevelVariable(String name, KPackage owner) {
        throw error();
    }

    public KMutableTopLevelVariable mutableTopLevelVariable(String name, KPackage owner) {
        throw error();
    }

    public KTopLevelExtensionProperty topLevelExtensionProperty(String name, KPackage owner, Class receiver) {
        throw error();
    }

    public KMutableTopLevelExtensionProperty mutableTopLevelExtensionProperty(String name, KPackage owner, Class receiver) {
        throw error();
    }

    private Error error() {
        throw new KotlinReflectionNotSupportedError();
    }
}