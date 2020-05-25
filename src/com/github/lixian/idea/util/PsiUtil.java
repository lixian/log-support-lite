package com.github.lixian.idea.util;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;

/**
 * @author lixian
 */
public class PsiUtil {

    private PsiUtil() {
    }

    private static final Logger log = Logger.getInstance(PsiUtil.class);

    static PsiElementFactory getFactory(PsiElement place) {
        return JavaPsiFacade.getInstance(place.getProject()).getElementFactory();
    }

    public static PsiElement getPlace(ExpressionContext context) {
        PsiFile psiFile = getPsiFile(context);
        int startOffset = context.getStartOffset();
        PsiDocumentManager.getInstance(psiFile.getProject()).commitAllDocuments();
        return psiFile.findElementAt(startOffset);
    }

    static boolean isThrowable(PsiMethodCallExpression expression, PsiType type) {
        if (type == null) {
            return false;
        }
        PsiClassType throwable = PsiType.getJavaLangThrowable(expression.getManager(),
                expression.getResolveScope());
        return throwable.isAssignableFrom(type);
    }

    static PsiClass classForPlace(PsiElement place) {
        return place instanceof PsiClass ? (PsiClass) place : com.intellij.psi.util.PsiUtil
                .getTopLevelClass(place);
    }

    /**
     * Creates a runnable that modifies the top-level class of the given place by adding a logger field.
     *
     * @param place The place to use as reference, either the top level class or a child of it.
     * @param field The field to add.
     * @return a runnable that modifies the top-level class of the given place by adding a logger field.
     */
    public static Runnable createFieldInserter(final PsiElement place, final PsiField field) {
        PsiFile file = place.getContainingFile();
        final Project project = file.getProject();
        final PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
        final Document document = manager.getDocument(file);

        if (document != null) {
            return new Runnable() {

                public void run() {
                    manager.commitDocument(document);

                    try {
                        PsiClass cls = classForPlace(place);
                        PsiElement brace = cls.getLBrace();

                        PsiElement addedField;
                        PsiField[] allFields = cls.getFields();
                        if (allFields.length == 0) {
                            addedField = cls.addAfter(field, brace);
                        } else {
                            addedField = addFieldBeforeAnchor(cls, field, allFields[0]);
                        }
                        shortenFQNames(addedField);
                    } finally {
                        manager.doPostponedOperationsAndUnblockDocument(document);
                    }
                }

                void shortenFQNames(PsiElement elementToFormat) {
                    try {
                        JavaCodeStyleManager javaStyle = JavaCodeStyleManager.getInstance(project);
                        javaStyle.shortenClassReferences(elementToFormat);
                    } catch (IncorrectOperationException e) {
                        log.error(e);
                    }
                }
            };
        }

        return null;
    }

    private static PsiElement addFieldBeforeAnchor(@NotNull PsiClass cls, @NotNull PsiField field,
            @NotNull PsiElement anchor) {
        final PsiElement addedField;
        addedField = cls.addBefore(field, anchor);

        return addedField;
    }

    public static PsiFile getPsiFile(ExpressionContext context) {
        Project project = context.getProject();
        //noinspection ConstantConditions
        return PsiDocumentManager.getInstance(project)
                .getPsiFile(context.getEditor().getDocument());
    }

    /**
     * Finds and returns a method call expression under the current caret.
     *
     * @param editor The editor to retrieve the caret position from.
     * @param file The underlying parsed file.
     * @return The call expression or 'null' if the caret is at a call expression.
     */
    @Nullable
    public static PsiMethodCallExpression findMethodCallExpressionAtCaret(Editor editor,
            PsiFile file) {
        final PsiElement psiUnderCaret = com.intellij.psi.util.PsiUtil.getElementAtOffset(file,
                editor.getCaretModel().getOffset());
        //noinspection unchecked
        return findElement(iterateParents(psiUnderCaret), PsiMethodCallExpression.class,
                PsiExpressionList.class, PsiBinaryExpression.class, PsiReferenceExpression.class,
                PsiIdentifier.class, PsiVariable.class, PsiLiteralExpression.class,
                PsiJavaToken.class);
    }

    /**
     * Finds an specific element type inside a given iteration.
     *
     * @param elements The elements to iterate.
     * @param expectedType The expected type to look for.
     * @param ignoredElementTypes A set of types that may be skipped in the Iterable.
     * @param <E> The type of the PsiElement to return.
     * @return The first occurrence of the expected type or 'null' if not found.
     */
    @SuppressWarnings("unchecked")
    private static <E extends PsiElement> E findElement(Iterable<PsiElement> elements,
            Class<E> expectedType, Class<? extends PsiElement>... ignoredElementTypes) {
        mainLoop:
        for (PsiElement e : elements) {
            Class<? extends PsiElement> elementClass = e.getClass();

            if (expectedType.isAssignableFrom(elementClass)) {
                return (E) e;
            }

            for (Class<? extends PsiElement> ignoredElement : ignoredElementTypes) {
                if (ignoredElement != null && ignoredElement.isAssignableFrom(elementClass)) {
                    continue mainLoop;
                }
            }

            break;
        }

        return null;
    }

    /**
     * Creates an iterable iterating all parents of the given element.
     *
     * @param element The element to use for iterating the parent tree.
     * @return An Iterable starting from the given element.
     */
    private static Iterable<PsiElement> iterateParents(final PsiElement element) {
        return () -> new Iterator<PsiElement>() {

            private PsiElement el = element;

            public boolean hasNext() {
                return el != null;
            }

            public PsiElement next() {
                try {
                    return el;
                } finally {
                    el = el.getParent();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
