package com.github.lixian.idea.inspections.logging;

import org.jetbrains.annotations.NotNull;

import com.github.lixian.idea.util.LogPsiUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;

/**
 * @author lixian
 */
@SuppressWarnings("WeakerAccess")
public class VerifyFormattedMessageInspection extends BaseInspection {

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

    @NotNull
    @Override
    protected String buildErrorString(Object... objects) {
        return "Verify formatted message failed";
    }

    @Override
    public BaseInspectionVisitor buildVisitor() {
        return new VerifyFormattedBaseInspectionVisitor();
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    private static class VerifyFormattedBaseInspectionVisitor extends BaseInspectionVisitor {

        private final LocalQuickFix appendPairsQuickFix = new AppendPairsToMessageQuickFix();

        private final LocalQuickFix appendPlaceholdersQuickFix = new AppendPlaceholdersToMessageQuickFix();

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression call) {
            super.visitMethodCallExpression(call);
            PsiReferenceExpression method = call.getMethodExpression();
            if (LogPsiUtil.isLoggerMethod(method)) {
                checkArgsMatch(call);
            }
        }

        private void checkArgsMatch(PsiMethodCallExpression call) {
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
                        registerMethodCallError(call, descriptionTemplate, appendPairsQuickFix,
                                appendPlaceholdersQuickFix);
                    } else {
                        registerMethodCallError(call, descriptionTemplate);
                    }
                }
            } else {
                // ignore, don't handle the first parameter is not direct string
            }
        }
    }
}
