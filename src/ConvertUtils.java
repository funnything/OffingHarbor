import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by zhouzhiyong on 15-7-24.
 */
public class ConvertUtils {
    /**
     * 基準ファイルから子を優先して探索する
     */
    public static VirtualFile traverseLayoutFileByName(String name, VirtualFile baseDir, VirtualFile file, VirtualFile traverseFrom) {
        VirtualFile parent = file.getParent();

        if (!file.isDirectory() && file.getName().equalsIgnoreCase(name + ".xml")) {
            if (parent != null && parent.getName().startsWith("layout")) {
                VirtualFile grandParent = parent.getParent();
                if (grandParent != null && grandParent.getName().equalsIgnoreCase("res")) {
                    return file;
                }
            }
        }

        String traverseFromUrl = traverseFrom != null ? traverseFrom.getUrl() : null;

        for (VirtualFile child : file.getChildren()) {
            if (!child.getUrl().equals(traverseFromUrl) && !child.getName().startsWith(".")) {
                VirtualFile layoutFile = traverseLayoutFileByName(name, baseDir, child, file);
                if (layoutFile != null) {
                    return layoutFile;
                }
            }
        }

        if (!file.getUrl().equals(baseDir.getUrl())) {
            if (parent != null) {
                if (!parent.getUrl().equals(traverseFromUrl)) {
                    return traverseLayoutFileByName(name, baseDir, parent, file);
                }
            }
        }

        return null;
    }
}
