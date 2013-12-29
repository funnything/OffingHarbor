import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by toyama.yosaku on 13/12/29.
 */
@State(name = "HelloPluginConfig", reloadable = true, storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/hello_plugin.xml", scheme = StorageScheme.DIRECTORY_BASED)
})
public class HelloPluginConfig implements PersistentStateComponent<HelloPluginConfig> {
    private Integer count;

    @Nullable
    public static HelloPluginConfig getInstance(Project project) {
        return ServiceManager.getService(project, HelloPluginConfig.class);
    }

    @Nullable
    @Override
    public HelloPluginConfig getState() {
        return this;
    }

    @Override
    public void loadState(HelloPluginConfig config) {
        XmlSerializerUtil.copyBean(config, this);
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public boolean isEmpty() {
        return count == null;
    }

    public void init() {
        this.count = 0;
    }

    public void increment() {
        this.count++;
    }
}
