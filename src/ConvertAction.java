import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.net.io.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by toyama.yosaku on 13/12/29.
 */
public class ConvertAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());

        if (file == null) {
            showError("Cannot find target file");
            return;
        }

        String extension = file.getExtension();

        if (extension != null && extension.equalsIgnoreCase("xml")) {
            if (!file.getParent().getName().startsWith("layout")) {
                showError(String.format("Selected file directory (%s) is seems not Android layout directory", file.getParent().getName()));
                return;
            }

            new ConvertConfigDialog(e.getProject(), file).show();
        } else if (extension != null && extension.equalsIgnoreCase("java")) {
            String name = extractLayoutFileNameFromJavaFile(file);

            if (name == null) {
                showError(String.format("Cannot find layout file id from [%s]", file.getName()));
                return;
            }

            VirtualFile layoutFile = ConvertUtils.traverseLayoutFileByName(name, e.getProject().getBaseDir(), file, null);

            if (layoutFile == null) {
                showError(String.format("Cannot find layout file for name [%s]", name));
                return;
            }

            new ConvertConfigDialog(e.getProject(), layoutFile).show();
        } else {
            showError(String.format("This file (%s) extension is not supported", file.getName()));
        }
    }

    private String extractLayoutFileNameFromJavaFile(VirtualFile file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()));

            Pattern pattern = Pattern.compile("R\\.layout\\.([a-z0-9_]+)");
            for (String l; (l = reader.readLine()) != null; ) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Util.closeQuietly(reader);
        }
    }



    private void showError(String content) {
        Notifications.Bus.notify(new Notification("OffingHarbor", "OffingHarbor", content, NotificationType.ERROR));
    }
}
