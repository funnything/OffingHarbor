package jp.funnything.offing_harbor;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Created by toyama.yosaku on 13/12/29.
 */
@State(name = "jp.funnything.offing_harbor.ConvertConfigRepository", reloadable = true, storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/offing_harbor_config.xml", scheme = StorageScheme.DIRECTORY_BASED)
})
public class ConvertConfigRepository implements PersistentStateComponent<ConvertConfig> {
    @Nullable
    public static ConvertConfigRepository getInstance(Project project) {
        return ServiceManager.getService(project, ConvertConfigRepository.class);
    }

    private ConvertConfig mConfig;

    @Nullable
    @Override
    public ConvertConfig getState() {
        return mConfig;
    }

    @Override
    public void loadState(ConvertConfig config) {
        mConfig = config;
    }
}
