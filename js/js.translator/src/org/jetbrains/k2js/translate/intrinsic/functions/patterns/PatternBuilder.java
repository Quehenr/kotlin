/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.k2js.translate.intrinsic.functions.patterns;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.FunctionDescriptor;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Talanov
 */
public final class PatternBuilder {

    @NotNull
    private static final NamePredicate JET = new NamePredicate("jet");

    private PatternBuilder() {
    }

    @NotNull
    public static DescriptorPredicate pattern(@NotNull NamePredicate checker, @NotNull String stringWithPattern) {
        List<NamePredicate> checkers = Lists.newArrayList(checker);
        checkers.addAll(parseStringAsCheckerList(stringWithPattern));
        return pattern(checkers);
    }

    @NotNull
    public static DescriptorPredicate pattern(@NotNull String stringWithPattern, @NotNull NamePredicate checker) {
        List<NamePredicate> checkers = Lists.newArrayList(parseStringAsCheckerList(stringWithPattern));
        checkers.add(checker);
        return pattern(checkers);
    }

    @NotNull
    public static DescriptorPredicate pattern(@NotNull String string) {
        List<NamePredicate> checkers = parseStringAsCheckerList(string);
        return pattern(checkers);
    }

    @NotNull
    private static List<NamePredicate> parseStringAsCheckerList(@NotNull String stringWithPattern) {
        String[] subPatterns = stringWithPattern.split("\\.");
        List<NamePredicate> checkers = Lists.newArrayList();
        for (String subPattern : subPatterns) {
            String[] validNames = subPattern.split("\\|");
            checkers.add(new NamePredicate(validNames));
        }
        return checkers;
    }

    @NotNull
    private static DescriptorPredicate pattern(@NotNull List<NamePredicate> checkers) {
        final List<NamePredicate> checkersWithPrefixChecker = Lists.newArrayList(JET);
        checkersWithPrefixChecker.addAll(checkers);
        return new DescriptorPredicate() {
            @Override
            public boolean apply(@NotNull FunctionDescriptor descriptor) {
                //TODO: no need to wrap if we check beforehand
                try {
                    return doApply(descriptor);
                }
                catch (IllegalArgumentException e) {
                    return false;
                }
            }

            private boolean doApply(@NotNull FunctionDescriptor descriptor) {
                List<Name> nameParts = DescriptorUtils.getFQName(descriptor).pathSegments();
                if (nameParts.size() != checkersWithPrefixChecker.size()) {
                    return false;
                }
                if (!allNamePartsValid(nameParts)) return false;
                return true;
            }

            private boolean allNamePartsValid(@NotNull List<Name> nameParts) {
                for (int i = 0; i < nameParts.size(); ++i) {
                    Name namePart = nameParts.get(i);
                    NamePredicate correspondingPredicate = checkersWithPrefixChecker.get(i);
                    if (!correspondingPredicate.apply(namePart)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    @NotNull
    public static DescriptorPredicate pattern(@NotNull final NamePredicate... checkers) {
        return pattern(Arrays.asList(checkers));
    }

    @NotNull
    public static DescriptorPredicateImpl pattern(@NotNull final String... names) {
        return new DescriptorPredicateImpl(names);
    }

    private static boolean isRootNamespace(DeclarationDescriptor declarationDescriptor) {
        return declarationDescriptor instanceof NamespaceDescriptor && DescriptorUtils.isRootNamespace((NamespaceDescriptor) declarationDescriptor);
    }

    public static class DescriptorPredicateImpl implements DescriptorPredicate {
        private final String[] names;

        private boolean receiverParameterExists;

        public DescriptorPredicateImpl(String... names) {
            this.names = names;
        }

        public DescriptorPredicateImpl receiverExists(boolean receiverParameterExists) {
            this.receiverParameterExists = receiverParameterExists;
            return this;
        }

        @Override
        public boolean apply(@NotNull FunctionDescriptor functionDescriptor) {
            if (functionDescriptor.getReceiverParameter().exists() != receiverParameterExists) {
                return false;
            }

            DeclarationDescriptor descriptor = functionDescriptor;
            int nameIndex = names.length - 1;
            do {
                if (!descriptor.getName().getName().equals(names[nameIndex--])) {
                    return false;
                }
                descriptor = descriptor.getContainingDeclaration();
                if (nameIndex == -1) {
                    return isRootNamespace(descriptor);
                }
            }
            while (descriptor != null && !isRootNamespace(descriptor));
            return false;
        }
    }
}
