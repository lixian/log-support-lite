package com.github.lixian.idea.livetemplates;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

/**
 * @author lixian
 */
public class ResolveSqlParam extends Macro {

    @Override
    public String getName() {
        return "resolveSqlParam";
    }

    @Override
    public String getPresentableName() {
        return getClass().getName();
    }

    @Nullable
    @Override
    public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {
        List<String> names = getNames(context);
        if (names.isEmpty()) {
            return new TextResult("()");
        }
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String name : names) {
            if (isFirst) {
                builder.append(pair(name));
                isFirst = false;
            } else {
                builder.append(" //\n");
                builder.append(".addValue");
                builder.append(pair(name));
            }
        }
        builder.append(";");
        return new TextResult(builder.toString());
    }

    private String pair(String name) {
        String wrap = "(\"%s\", %s)";
        return String.format(wrap, name, name);
    }

    @NotNull
    private List<String> getNames(ExpressionContext context) {
        List<String> result = new ArrayList<>();
        for (PsiElement place = context.getPsiElementAtStartOffset(); place != null; place = place
                .getParent()) {
            if (place instanceof PsiMethod) {
                PsiParameter[] parameters = ((PsiMethod) place).getParameterList().getParameters();
                for (PsiParameter parameter : parameters) {
                    result.add(parameter.getName());
                }
            }
        }
        return result;
    }
}
