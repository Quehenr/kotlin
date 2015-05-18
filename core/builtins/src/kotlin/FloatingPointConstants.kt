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

package kotlin

/**
 * An interface implemented by companion objects of floating-point types.
 */
public interface FloatingPointConstants<T> {
    /**
     * A constant holding the positive infinity value.
     */
    public val POSITIVE_INFINITY: T

    /**
     * A constant holding the negative infinity value.
     */
    public val NEGATIVE_INFINITY: T

    /**
     * A constant holding the "not a number" value.
     */
    public val NaN: T
}