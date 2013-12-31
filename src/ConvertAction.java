import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by toyama.yosaku on 13/12/29.
 */
public class ConvertAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
        assert editor != null;

        if (!(editor instanceof EditorImpl)) {
            showError("Selected text editorImpl type is unknown: " + editor.getClass().toString());
            return;
        }

        EditorImpl editorImpl = (EditorImpl) editor;
        VirtualFile file = editorImpl.getVirtualFile().getCanonicalFile();
        assert file != null;

        String extension = file.getExtension();
        assert extension != null;

        if (!extension.equalsIgnoreCase("xml")) {
            showError("Not supported file extension: " + extension);
            return;
        }

        if (!file.getParent().getName().startsWith("layout")) {
            showError("Selected file seems not Android layout file structure: " + file.getParent().getName());
            return;
        }

        new ConvertConfigDialog(e.getProject()).show();
    }

    private void showError(String content) {
        Notifications.Bus.notify(new Notification("OffingHarbor", "OffingHarbor", content, NotificationType.ERROR));
    }
}
