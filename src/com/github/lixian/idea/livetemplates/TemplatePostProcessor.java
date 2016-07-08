package com.github.lixian.idea.livetemplates;

import org.jetbrains.annotations.Nls;

import com.github.lixian.idea.util.PsiUtil;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateOptionalProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;

/**
 * @author lixian
 */
public class TemplatePostProcessor implements TemplateOptionalProcessor {

    private static final Logger log = Logger.getInstance(TemplatePostProcessor.class);

    private static final Key<Runnable> PENDING_RUNNABLE = Key
            .create("LOG_SUPPORT_LITE_PENDING_RUNNABLE");

    private static final Key<Boolean> PENDING_REMOVE = Key
            .create("LOG_SUPPORT_LITE_PENDING_REMOVE");

    /**
     * Schedules a runnable to be executed in the post processing step.
     *
     * @param file	 the file to run the runnable with.
     * @param runnable the runnable to schedule.
     * @param replace  whether the runnable should be added to the chain
     *                 or replace the chain of existing runnables.
     */
    static void schedule(PsiFile file, final Runnable runnable, boolean replace) {
        final Runnable existing = file.getUserData(PENDING_RUNNABLE);
        if (!replace && existing != null) {
            file.putUserData(PENDING_RUNNABLE, () -> {
                existing.run();
                runnable.run();
            });
        } else {
            file.putUserData(PENDING_RUNNABLE, runnable);
        }
    }

    static void markPendingRemove(PsiFile file) {
        file.putUserData(PENDING_REMOVE, true);
    }

    @Override
    public void processText(Project project, Template template, Document document,
            RangeMarker rangeMarker, Editor editor) {
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (file != null) {
            processPendingRunnable(file);
            removeNonExistingTypes(file, editor);
        }
    }

    private void processPendingRunnable(PsiFile file) {
        // Process pending runnable
        Runnable runnable = file.getUserData(PENDING_RUNNABLE);
        if (runnable != null) {
            try {
                runnable.run();
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Throwable t) {
                log.error(t);
            } finally {
                file.putUserData(PENDING_RUNNABLE, null);
            }
        }
    }

    private void removeNonExistingTypes(PsiFile file, Editor editor) {
        Boolean pendingRemove = file.getUserData(PENDING_REMOVE);
        if (pendingRemove == null || !pendingRemove) {
            return;
        }

        try {
            removeEmpty(file, editor);
        } finally {
            file.putUserData(PENDING_REMOVE, null);
        }
    }

    private void removeEmpty(PsiFile file, Editor editor) {
        PsiMethodCallExpression method = PsiUtil.findMethodCallExpressionAtCaret(editor, file);
        if (method == null) {
            return;
        }
        PsiExpressionList args = method.getArgumentList();
        PsiExpression[] expressions = args.getExpressions();
        for (PsiExpression e : expressions) {
            if (e.getType() == null) {
                e.delete();
            }
        }
    }

    @Nls
    @Override
    public String getOptionName() {
        return "option.name.log.support.lite";
    }

    @Override
    public boolean isEnabled(Template template) {
        return true;
    }

    @Override
    public void setEnabled(Template template, boolean b) {
    }

    @Override
    public boolean isVisible(Template template) {
        return false;
    }
}
