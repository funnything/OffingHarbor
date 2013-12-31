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
        HelloPluginConfig config = HelloPluginConfig.getInstance(e.getProject());
        assert config != null;

        if (config.isEmpty()) {
            config.init();
        }

        int count = config.getCount();

        String content = String.format("Hello, this is %d content!", count);
        Notifications.Bus.notify(new Notification("sample", "Hello title!", content, NotificationType.INFORMATION));

        config.increment();

        new ConvertDialog(e.getProject()).show();
    }
}
