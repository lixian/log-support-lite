package com.github.lixian.idea.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;

/**
 * @author lixian
 */
public class LogPsiUtil {

    private static final String LOGGER_CLASS = "org.slf4j.Logger";
    private static final String LOGGER_FACTORY_METHOD = "org.slf4j.LoggerFactory.getLogger(%s.class)";
    private static final ImmutableSet<String> METHODS = ImmutableSet.of("info", "debug", "trace",
            "warn", "error");

    @NotNull
    public static PsiType getLoggerType(PsiElement place) {
        PsiElementFactory factory = PsiUtil.getFactory(place);
        return factory.createTypeFromText(LOGGER_CLASS, place);
    }

    @NotNull
    public static PsiField createLoggerField(PsiElement place, String loggerName) {
        PsiElementFactory factory = PsiUtil.getFactory(place);
        PsiType loggerType = getLoggerType(place);
        PsiField field = factory.createField(loggerName, loggerType);
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            modifierList.setModifierProperty(PsiModifier.STATIC, true);
            modifierList.setModifierProperty(PsiModifier.FINAL, true);
        }

        PsiClass clz = PsiUtil.classForPlace(place);
        String expression = String.format(LOGGER_FACTORY_METHOD, clz.getQualifiedName());
        field.setInitializer(factory.createExpressionFromText(expression, clz.getContext()));
        return field;
    }

    public static boolean isLoggerMethod(PsiReferenceExpression method) {
        return checkMethodName(method) && isQualifierLogger(method);
    }

    private static boolean checkMethodName(PsiReferenceExpression method) {
        final PsiElement lastChild = method.getLastChild();
        return lastChild != null && METHODS.contains(lastChild.getText());
    }

    private static boolean isQualifierLogger(PsiReferenceExpression method) {
        PsiExpression qualifier = method.getQualifierExpression();
        if (qualifier == null) {
            return false;
        }
        PsiType qualifierType = qualifier.getType();
        return qualifierType != null && getLoggerType(method).isAssignableFrom(qualifierType);
    }

    public static int getFilledCount(String messageText) {
        return StringUtils.countMatches(messageText, "{}");
    }

    public static List<PsiExpression>
            getArgsExcludeMsgAndLastThrowable(PsiMethodCallExpression call, PsiExpression[] args) {
        List<PsiExpression> list = Arrays.asList(args);
        if (list.size() <= 1) {
            return Collections.emptyList();
        }
        boolean throwable = isLastElementThrowable(call, args);
        if (throwable) {
            return list.subList(1, list.size() - 1);
        } else {
            return list.subList(1, list.size());
        }
    }

    private static boolean isLastElementThrowable(PsiMethodCallExpression call,
            PsiExpression[] args) {
        PsiExpression last = args[args.length - 1];
        return PsiUtil.isThrowable(call, last.getType());
    }
}
