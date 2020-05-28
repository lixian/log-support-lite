package com.github.lixian.idea.inspections.logging;

import org.jetbrains.annotations.NotNull;

import com.github.lixian.idea.util.LogPsiUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;

/**
 * @author lixian
 */
public class VerifyFormattedMessageInspection extends MyBaseJavaLocalInspectionTool {

    private final LocalQuickFix appendPairsQuickFix = new AppendPairsToMessageQuickFix();

    private final LocalQuickFix appendPlaceholdersQuickFix = new AppendPlaceholdersToMessageQuickFix();

    @NotNull
    @Override
    public String getDisplayName() {
        return "Verify formatted message";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "VerifyFormattedMessage";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression call) {
                PsiReferenceExpression method = call.getMethodExpression();
                if (LogPsiUtil.isLoggerMethod(method)) {
                    checkArgsMatch(call, holder);
                }
            }
        };
    }

    private void checkArgsMatch(PsiMethodCallExpression call, @NotNull ProblemsHolder holder) {
        PsiExpression[] args = call.getArgumentList().getExpressions();
        if (args.length == 0) {
            return;
        }

        String messageText = args[0].getText();
        //noinspection StatementWithEmptyBody
        if (messageText.startsWith("\"")) {
            int filledCount = LogPsiUtil.getFilledCount(messageText);
            int argsCount = LogPsiUtil.getArgsExcludeMsgAndLastThrowable(call, args).size();

            if (filledCount != argsCount) {
                String title = "The formatted log message expected %d arguments, passed %d.";
                String descriptionTemplate = String.format(title, filledCount, argsCount);

                if (filledCount < argsCount) {
                    holder.registerProblem(call, descriptionTemplate, appendPairsQuickFix,
                            appendPlaceholdersQuickFix);
                } else {
                    holder.registerProblem(call, descriptionTemplate);
                }
            }
        } else {
            // ignore, don't handle the first parameter is not direct string
        }
    }
}
