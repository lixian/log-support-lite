package com.github.lixian.idea.livetemplates;

import org.jetbrains.annotations.NotNull;

import com.github.lixian.idea.util.PsiUtil;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.codeInsight.template.macro.VariableOfTypeMacro;

/**
 * @author lixian
 */
public class ResolveOptionalVariableOfType extends VariableOfTypeMacro {

    @Override
    public String getName() {
        return "resolveOptionalVariableOfType";
    }

    @Override
    public String getPresentableName() {
        return getClass().getName();
    }

    @Override
    public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {
        Result result = super.calculateResult(expressions, context);
        if (result == null) {
            TemplatePostProcessor.markPendingRemove(PsiUtil.getPsiFile(context));
            return new TextResult("");
        }
        return result;
    }
}
