package com.github.lixian.idea.inspections.logging;

import java.util.List;
import java.util.StringJoiner;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiExpression;

/**
 * @author lixian
 */
public class AppendPairsToMessageQuickFix extends AppendToMessageQuickFix {

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Append 'arg={}, ...' to message";
    }

    @Override
    @NotNull
    protected String createAppendMessageText(List<PsiExpression> argsNeedToFill) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PsiExpression expression : argsNeedToFill) {
            String name = parseName(expression);
            joiner.add(name + "={}");
        }
        return joiner.toString();
    }

    private String parseName(PsiExpression expression) {
        String text = expression.getText();
        if (text.contains("\"")) {
            return "s";
        }
        return text;
    }
}
