import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by toyama.yosaku on 13/12/28.
 */
public class HelloAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Notifications.Bus.notify(new Notification("sample", "Hello title!", "Hello, this is content!", NotificationType.INFORMATION));
    }
}
