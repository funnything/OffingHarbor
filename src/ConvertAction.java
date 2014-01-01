import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by toyama.yosaku on 13/12/29.
 */
public class ConvertAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
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

        new ConvertConfigDialog(e).show();
    }

    private void showError(String content) {
        Notifications.Bus.notify(new Notification("OffingHarbor", "OffingHarbor", content, NotificationType.ERROR));
    }
}
