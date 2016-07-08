package com.github.lixian.idea.inspections;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author lixian
 */
public class InspectionsProvider implements InspectionToolProvider {

    @Override
    public Class[] getInspectionClasses() {
        return new Class[] { VerifyFormattedMessageInspection.class };
    }
}
