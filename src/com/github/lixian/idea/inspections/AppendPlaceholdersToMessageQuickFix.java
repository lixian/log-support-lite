package com.github.lixian.idea.inspections;

import java.util.List;
import java.util.StringJoiner;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiExpression;

/**
 * @author lixian
 */
public class AppendPlaceholdersToMessageQuickFix extends AppendToMessageQuickFix {

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Append '{} ...' to message";
    }

    @NotNull
    @Override
    protected String createAppendMessageText(List<PsiExpression> argsNeedToFill) {
        StringJoiner joiner = new StringJoiner(" ");
        for (PsiExpression e : argsNeedToFill) {
            joiner.add("{}");
        }
        return joiner.toString();
    }
}
