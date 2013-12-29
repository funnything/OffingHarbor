import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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

        List<AndroidViewInfo> infos = extractViewInfos(editorImpl.getDocument().getText());
        String javaCode = generateJavaCode(infos);

        CopyPasteManager.getInstance().setContents(new StringSelection(javaCode));

        Notifications.Bus.notify(new Notification("OffingHarbor", "OffingHarbor", "Code is copied to clipboard", NotificationType.INFORMATION));
    }

    private List<AndroidViewInfo> extractViewInfos(String text) {
        try {
            return traverseViewInfos(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(text))));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<AndroidViewInfo> traverseViewInfos(Node node) {
        List<AndroidViewInfo> infos = Lists.newArrayList();

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Node idNode = node.getAttributes().getNamedItem("android:id");

            if (idNode != null) {
                String value = idNode.getNodeValue();
                String[] values = value.split("/");

                if (values.length != 2) {
                    throw new IllegalStateException("android:id value is invalid");
                }

                String[] elements = node.getNodeName().split("\\.");

                infos.add(new AndroidViewInfo(elements[elements.length - 1], values[1]));
            }
        }

        NodeList children = node.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            infos.addAll(traverseViewInfos(children.item(index)));
        }

        return infos;
    }

    private String generateJavaCode(List<AndroidViewInfo> infos) {
        StringBuilder fieldJavaCode = new StringBuilder();
        StringBuilder methodJavaCode = new StringBuilder();

        String NL = "\n";

        methodJavaCode.append("private void assignViews() {" + NL);

        for (AndroidViewInfo info : infos) {
            fieldJavaCode.append(String.format("private %s %s;" + NL, info.type, info.getJavaSymbolName()));
            methodJavaCode.append(String.format("    %s = (%s) findViewById(R.id.%s);" + NL, info.getJavaSymbolName(), info.type, info.id));
        }

        methodJavaCode.append("}" + NL);

        return fieldJavaCode.toString() + NL + methodJavaCode.toString();
    }

    private void showError(String content) {
        Notifications.Bus.notify(new Notification("OffingHarbor", "OffingHarbor", content, NotificationType.ERROR));
    }

    private static class AndroidViewInfo {
        public String type;
        public String id;

        private AndroidViewInfo(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public String getJavaSymbolName() {
            return snakeCaseToCamelCase(id);
        }

        private String snakeCaseToCamelCase(String snakeCase) {
            List<String> capitalized = Lists.newArrayList();

            boolean isFirst = true;
            for (String s : snakeCase.split("_")) {
                if (isFirst) {
                    capitalized.add(s);
                } else {
                    capitalized.add(StringUtils.capitalize(s));
                }

                isFirst = false;
            }

            return StringUtils.join(capitalized, "");
        }

        @Override
        public String toString() {
            return "AndroidViewInfo{" +
                    "type='" + type + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }
}
