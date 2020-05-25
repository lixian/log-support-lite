package com.github.lixian.idea.inspections;

import com.github.lixian.idea.inspections.logging.VerifyFormattedMessageInspection;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;

/**
 * @author lixian
 */
public class InspectionsProvider implements InspectionToolProvider {

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends LocalInspectionTool>[] getInspectionClasses() {
        return new Class[] {VerifyFormattedMessageInspection.class};
    }
}
