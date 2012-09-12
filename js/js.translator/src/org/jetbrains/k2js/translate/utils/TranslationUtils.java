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

package org.jetbrains.k2js.translate.utils;

import com.google.dart.compiler.backend.js.ast.*;
import com.intellij.openapi.util.Pair;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.FunctionDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertyDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertyGetterDescriptor;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.k2js.translate.context.TemporaryVariable;
import org.jetbrains.k2js.translate.context.TranslationContext;
import org.jetbrains.k2js.translate.general.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.k2js.translate.utils.BindingUtils.getFunctionDescriptorForOperationExpression;
import static org.jetbrains.k2js.translate.utils.JsAstUtils.*;

/**
 * @author Pavel Talanov
 */
public final class TranslationUtils {
    private TranslationUtils() {
    }

    public static boolean isCacheNeeded(@NotNull JsExpression expression) {
        return !(expression instanceof JsNameRef) || ((JsNameRef) expression).getQualifier() != null;
    }

    @NotNull
    public static Pair<JsVars.JsVar, JsNameRef> createTemporaryIfNeed(@NotNull JsExpression expression,
            @NotNull TranslationContext context) {
        // don't create temp variable for simple expression
        if (isCacheNeeded(expression)) {
            return context.dynamicContext().createTemporary(expression);
        }
        else {
            return new Pair<JsVars.JsVar, JsNameRef>(null, (JsNameRef) expression);
        }
    }

    @NotNull
    public static JsPropertyInitializer translateFunctionAsEcma5PropertyDescriptor(@NotNull JsFunction function,
            @NotNull FunctionDescriptor descriptor,
            @NotNull TranslationContext context) {
        if (JsDescriptorUtils.isExtension(descriptor)) {
            return translateExtensionFunctionAsEcma5DataDescriptor(function, descriptor, context);
        }
        else {
            JsStringLiteral getOrSet = context.program().getStringLiteral(descriptor instanceof PropertyGetterDescriptor ? "get" : "set");
            return new JsPropertyInitializer(getOrSet, function);
        }
    }

    @NotNull
    private static JsPropertyInitializer translateExtensionFunctionAsEcma5DataDescriptor(@NotNull JsFunction function,
            @NotNull FunctionDescriptor descriptor, @NotNull TranslationContext context) {
        JsObjectLiteral meta = createDataDescriptor(function, descriptor.getModality().isOverridable());
        return new JsPropertyInitializer(context.getNameForDescriptor(descriptor).makeRef(), meta);
    }

    @NotNull
    public static JsBinaryOperation isNullCheck(@NotNull JsExpression expressionToCheck) {
        return nullCheck(expressionToCheck, false);
    }

    @NotNull
    public static JsBinaryOperation isNotNullCheck(@NotNull JsExpression expressionToCheck) {
        return nullCheck(expressionToCheck, true);
    }

    @NotNull
    public static JsBinaryOperation notNullConditionalTestExpression(@NotNull TemporaryVariable cachedValue) {
        return and(inequality(cachedValue.assignmentExpression(), JsLiteral.NULL),
                   inequality(cachedValue.reference(), JsLiteral.UNDEFINED));
    }

    @NotNull
    public static JsBinaryOperation nullCheck(@NotNull JsExpression expressionToCheck, boolean isNegated) {
        JsBinaryOperator operator = isNegated ? JsBinaryOperator.REF_NEQ : JsBinaryOperator.REF_EQ;
        return new JsBinaryOperation(isNegated ? JsBinaryOperator.AND : JsBinaryOperator.OR,
                                     new JsBinaryOperation(operator, expressionToCheck, JsLiteral.NULL),
                                     new JsBinaryOperation(operator, expressionToCheck, JsLiteral.UNDEFINED));
    }

    //@NotNull
    //public static JsExpression nullConditional(@NotNull JetExpression expression, @NotNull TranslationContext context, @NotNull JsExpression elseExpression, boolean isNegated) {
    //    return nullConditional(testExpression, context, elseExpression, isNegated);
    //}

    @NotNull
    public static JsExpression notNullConditional(
            @NotNull JsExpression expression,
            @NotNull TranslationContext context,
            @NotNull JsExpression elseExpression
    ) {
        JsExpression testExpression;
        JsExpression thenExpression;
        if (isCacheNeeded(expression)) {
            TemporaryVariable cachedValue = context.declareTemporary(expression);
            testExpression = notNullConditionalTestExpression(cachedValue);
            thenExpression = cachedValue.reference();
        }
        else {
            testExpression = isNotNullCheck(expression);
            thenExpression = expression;
        }

        return new JsConditional(testExpression, thenExpression, elseExpression);
    }

    @NotNull
    public static JsExpression notNullConditional(
            @NotNull JetExpression expression,
            @NotNull JsExpression elseExpression,
            @NotNull TranslationContext context
    ) {
        return notNullConditional(Translation.translateAsExpression(expression, context), elseExpression, context);
    }

    @NotNull
    public static JsConditional notNullConditional(
            @NotNull JsExpression expression,
            @NotNull JsExpression elseExpression,
            @NotNull TranslationContext context
    ) {
        JsExpression testExpression;
        JsExpression thenExpression;
        if (isCacheNeeded(expression)) {
            TemporaryVariable cachedValue = context.declareTemporary(expression);
            testExpression = notNullConditionalTestExpression(cachedValue);
            thenExpression = cachedValue.reference();
        }
        else {
            testExpression = isNotNullCheck(expression);
            thenExpression = expression;
        }

        return new JsConditional(testExpression, thenExpression, elseExpression);
    }

    @NotNull
    public static JsExpression notNullConditional(
            @NotNull JetExpression expression,
            @NotNull JsExpression elseExpression,
            @NotNull TranslationContext context
    ) {
        return notNullConditional(Translation.translateAsExpression(expression, context), elseExpression, context);
    }

    @NotNull
    public static JsConditional notNullConditional(
            @NotNull JsExpression expression,
            @NotNull JsExpression elseExpression,
            @NotNull TranslationContext context
    ) {
        JsExpression testExpression;
        JsExpression thenExpression;
        if (isCacheNeeded(expression)) {
            TemporaryVariable cachedValue = context.declareTemporary(expression);
            testExpression = notNullConditionalTestExpression(cachedValue);
            thenExpression = cachedValue.reference();
        }
        else {
            testExpression = isNotNullCheck(expression);
            thenExpression = expression;
        }

        return new JsConditional(testExpression, thenExpression, elseExpression);
    }

    @NotNull
    public static List<JsExpression> translateArgumentList(@NotNull TranslationContext context,
            @NotNull List<? extends ValueArgument> jetArguments) {
        if (jetArguments.isEmpty()) {
            return Collections.emptyList();
        }

        List<JsExpression> jsArguments = new SmartList<JsExpression>();
        for (ValueArgument argument : jetArguments) {
            jsArguments.add(translateArgument(context, argument));
        }
        return jsArguments;
    }

    @NotNull
    private static JsExpression translateArgument(@NotNull TranslationContext context, @NotNull ValueArgument argument) {
        JetExpression jetExpression = argument.getArgumentExpression();
        assert jetExpression != null : "Argument with no expression";
        return Translation.translateAsExpression(jetExpression, context);
    }

    @NotNull
    public static JsNameRef backingFieldReference(@NotNull TranslationContext context,
            @NotNull PropertyDescriptor descriptor) {
        JsName backingFieldName = context.getNameForDescriptor(descriptor);
        return new JsNameRef(backingFieldName, JsLiteral.THIS);
    }

    @NotNull
    public static JsExpression assignmentToBackingField(@NotNull TranslationContext context,
            @NotNull PropertyDescriptor descriptor,
            @NotNull JsExpression assignTo) {
        JsNameRef backingFieldReference = backingFieldReference(context, descriptor);
        return assignment(backingFieldReference, assignTo);
    }

    @Nullable
    public static JsExpression translateInitializerForProperty(@NotNull JetProperty declaration,
            @NotNull TranslationContext context) {
        JsExpression jsInitExpression = null;
        JetExpression initializer = declaration.getInitializer();
        if (initializer != null) {
            jsInitExpression = Translation.translateAsExpression(initializer, context);
        }
        return jsInitExpression;
    }

    @NotNull
    public static JsNameRef getQualifiedReference(@NotNull TranslationContext context, @NotNull DeclarationDescriptor descriptor) {
        return new JsNameRef(context.getNameForDescriptor(descriptor), context.getQualifierForDescriptor(descriptor));
    }

    @NotNull
    public static List<JsExpression> translateExpressionList(@NotNull TranslationContext context,
            @NotNull List<JetExpression> expressions) {
        List<JsExpression> result = new ArrayList<JsExpression>();
        for (JetExpression expression : expressions) {
            result.add(Translation.translateAsExpression(expression, context));
        }
        return result;
    }

    @NotNull
    public static JsExpression translateBaseExpression(@NotNull TranslationContext context,
            @NotNull JetUnaryExpression expression) {
        JetExpression baseExpression = PsiUtils.getBaseExpression(expression);
        return Translation.translateAsExpression(baseExpression, context);
    }

    @NotNull
    public static JsExpression translateLeftExpression(@NotNull TranslationContext context,
            @NotNull JetBinaryExpression expression) {
        return Translation.translateAsExpression(expression.getLeft(), context);
    }

    @NotNull
    public static JsExpression translateRightExpression(@NotNull TranslationContext context,
            @NotNull JetBinaryExpression expression) {
        JetExpression rightExpression = expression.getRight();
        assert rightExpression != null : "Binary expression should have a right expression";
        return Translation.translateAsExpression(rightExpression, context);
    }

    public static boolean hasCorrespondingFunctionIntrinsic(@NotNull TranslationContext context,
            @NotNull JetOperationExpression expression) {
        FunctionDescriptor operationDescriptor = getFunctionDescriptorForOperationExpression(context.bindingContext(), expression);

        if (operationDescriptor == null) return true;
        if (context.intrinsics().getFunctionIntrinsics().getIntrinsic(operationDescriptor).exists()) return true;

        return false;
    }

    @NotNull
    public static List<JsExpression> generateCallArgumentList(@NotNull JsExpression receiver, @NotNull List<JsExpression> arguments) {
        if (arguments.isEmpty()) {
            return Collections.singletonList(receiver);
        }

        List<JsExpression> argumentList = new ArrayList<JsExpression>(1 + arguments.size());
        argumentList.add(receiver);
        argumentList.addAll(arguments);
        return argumentList;
    }
}
