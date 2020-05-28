package com.github.lixian.idea.inspections.logging;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.CustomSuppressableInspectionTool;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.psi.PsiElement;
import com.siyeh.ig.GroupDisplayNameUtil;

/**
 * Copy to local for com.intellij.codeInspection.BaseJavaLocalInspectionTool is deprecated
 *
 * @author lixian
 */
public abstract class MyBaseJavaLocalInspectionTool extends AbstractBaseJavaLocalInspectionTool implements
        CustomSuppressableInspectionTool {

    @Override
    public SuppressIntentionAction[] getSuppressActions(PsiElement element) {
        String shortName = getShortName();
        HighlightDisplayKey key = HighlightDisplayKey.find(shortName);
        if (key == null) {
            throw new AssertionError("HighlightDisplayKey.find(" + shortName + ") is null. Inspection: " + getClass());
        }
        return SuppressManager.getInstance().createSuppressActions(key);
    }

    @NotNull
    @Override
    public String getGroupDisplayName() {
        return GroupDisplayNameUtil.getGroupDisplayName(getClass());
    }
}
