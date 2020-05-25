package com.github.lixian.idea.inspections.logging;

import java.util.List;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import com.github.lixian.idea.util.LogPsiUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;

/**
 * @author lixian
 */
public abstract class AppendToMessageQuickFix implements LocalQuickFix {

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();

        if (!(element instanceof PsiMethodCallExpression)) {
            return;
        }
        PsiMethodCallExpression call = (PsiMethodCallExpression) element;
        PsiExpression[] expressions = call.getArgumentList().getExpressions();
        if (expressions.length <= 1) {
            return;
        }

        PsiExpression msg = expressions[0];
        int filledCount = LogPsiUtil.getFilledCount(msg.getText());

        List<PsiExpression> args = LogPsiUtil.getArgsExcludeMsgAndLastThrowable(call, expressions);
        if (filledCount >= args.size()) {
            return;
        }

        List<PsiExpression> argsNeedToFill = args.subList(filledCount, args.size());
        String append = createAppendMessageText(argsNeedToFill);
        String newText = concat(msg, append);
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiExpression expressionFromText = factory.createExpressionFromText(newText, call);
        msg.replace(expressionFromText);
    }

    @NotNull
    private String concat(PsiExpression msg, String append) {
        String removeLastQuota = msg.getText().replaceAll("\"$", "");
        if (removeLastQuota.length() == 1) { // msg is ""
            return removeLastQuota + append + '"';
        } else if (removeLastQuota.endsWith(" ")) {
            return removeLastQuota + append + '"';
        } else {
            return removeLastQuota + " " + append + '"';
        }
    }

    @NotNull
    protected abstract String createAppendMessageText(List<PsiExpression> argsNeedToFill);

}
