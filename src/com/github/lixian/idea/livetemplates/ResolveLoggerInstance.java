package com.github.lixian.idea.livetemplates;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.lixian.idea.util.LogPsiUtil;
import com.github.lixian.idea.util.PsiUtil;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.JavaPsiElementResult;
import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.codeInsight.template.macro.MacroUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;

/**
 * @author lixian
 */
public class ResolveLoggerInstance extends Macro {

    private static final String DEFAULT_LOGGER_NAME = "logger";

    @Override
    public String getName() {
        return "resolveLoggerInstance";
    }

    @Override
    public String getPresentableName() {
        return getClass().getName();
    }

    @Nullable
    @Override
    public Result calculateResult(@NotNull Expression[] expressions, ExpressionContext context) {
        PsiElement place = PsiUtil.getPlace(context);

        PsiVariable logger = findLoggerField(place);
        if (logger == null) {
            String loggerName = parseLoggerName(expressions, context, DEFAULT_LOGGER_NAME);
            logger = createLogger(place, loggerName);
        }
        if (logger == null) {
            return new TextResult(getDefaultValue());
        }
        return new JavaPsiElementResult(logger);
    }

    @NotNull
    @Override
    public String getDefaultValue() {
        return DEFAULT_LOGGER_NAME;
    }

    private PsiVariable findLoggerField(PsiElement place) {
        PsiType loggerType = LogPsiUtil.getLoggerType(place);
        PsiVariable[] variables = MacroUtil.getVariablesVisibleAt(place, "");
        for (PsiVariable var : variables) {
            PsiType type = var.getType();
            if (loggerType.isAssignableFrom(type)) {
                return var;
            }
        }
        return null;
    }

    private String parseLoggerName(Expression[] expressions, ExpressionContext context,
            String defaultValue) {
        if (expressions.length < 1) {
            return defaultValue;
        }
        Expression expression = expressions[0];
        Result result = expression.calculateResult(context);
        if (result == null) {
            return defaultValue;
        }
        return result.toString();
    }

    private PsiVariable createLogger(PsiElement place, String loggerName) {
        PsiFile file = place.getContainingFile();
        if (!file.isWritable()) {
            return null;
        }
        PsiField field = LogPsiUtil.createLoggerField(place, loggerName);
        TemplatePostProcessor.schedule(file, PsiUtil.createFieldInserter(place, field), true);
        return field;
    }
}
