package com.github.lixian.idea.livetemplates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;

/**
 * @author lixian
 */
public class TemplatesProvider implements DefaultLiveTemplatesProvider {

    private static final String[] TEMPLATES = { "/liveTemplates/logsupport",
            "/liveTemplates/sqlparameters", };

    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return TEMPLATES;
    }

    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return null;
    }
}
