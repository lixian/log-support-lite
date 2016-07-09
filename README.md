# Log Support Lite Plugin

This is an IntelliJ IDEA plugin. <https://plugins.jetbrains.com/plugin/8463>

Clone core features from "Log Support" which is not maintained for a long time.

Optimized only for SLF4J.

- Support live templates: logt, logd, logi, logw, loge. </li>
- Support inspection that detects mismatched argument count for log message format,
    and the last throwable argument is excluded now. :)
- Add quick fixes when the count of placeholders is not enough.

PS: Support 'sqlp' to create a MapSqlParameterSource with all parameter filled.

## Development environment settings

For now, you can't *Open* a new project without `.iml` and `.idea` as plugin project.

- File | New | Project... | IntelliJ Platform Plugin | Next
- Input the existing project name and location, and choose overwrite if there are
    config files existing.
