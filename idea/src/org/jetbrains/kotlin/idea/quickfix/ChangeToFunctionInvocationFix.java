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

package org.jetbrains.kotlin.idea.quickfix;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.idea.JetBundle;
import org.jetbrains.kotlin.psi.JetExpression;
import org.jetbrains.kotlin.psi.JetFile;

import static org.jetbrains.kotlin.psi.PsiPackage.JetPsiFactory;

public class ChangeToFunctionInvocationFix extends JetIntentionAction<JetExpression> {

    public ChangeToFunctionInvocationFix(@NotNull JetExpression element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return JetBundle.message("change.to.function.invocation");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return JetBundle.message("change.to.function.invocation");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, JetFile file) throws IncorrectOperationException {
        JetExpression reference = (JetExpression) element.copy();
        element.replace(JetPsiFactory(file).createExpression(reference.getText() + "()"));
    }

    public static JetSingleIntentionActionFactory createFactory() {
        return new JetSingleIntentionActionFactory() {
            @Override
            public JetIntentionAction<JetExpression> createAction(Diagnostic diagnostic) {
                if (diagnostic.getPsiElement() instanceof JetExpression) {
                    return new ChangeToFunctionInvocationFix((JetExpression) diagnostic.getPsiElement());
                }
                return null;
            }
        };
    }
}
