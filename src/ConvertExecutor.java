import com.google.common.collect.Lists;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.io.Util;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by toyama.yosaku on 13/12/31.
 */
public class ConvertExecutor {
    private static class AndroidViewInfo {
        public String type;
        public String id;

        private AndroidViewInfo(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public String getJavaSymbolName(ConvertConfig config) {
            switch (config.prefix) {
                case NONE:
                    return snakeCaseToCamelCase(id);
                case MEMBER:
                    return snakeCaseToCamelCase("m_" + id);
                case UNDERSCORE:
                    return "_" + snakeCaseToCamelCase(id);
                default:
                    throw new IllegalStateException("assert");
            }
        }

        // 型名が一致する かつ this.type の親クラスであれば
        public String findOptimalType(Tree viewNameTree) {
            List<String> split = Arrays.asList(id.split("_"));

            while (!split.isEmpty()) {
                String candidate = capitalizeJoin(split);

                if (traverseOptimalType(viewNameTree, candidate, type, false)) {
                    return candidate;
                }

                split = split.subList(1, split.size());
            }

            return type;
        }

        private String capitalizeJoin(List<String> list) {
            List<String> capitalized = Lists.newArrayList();

            for (String s : list) {
                capitalized.add(StringUtils.capitalize(s));
            }

            return StringUtils.join(capitalized, "");
        }

        private boolean traverseOptimalType(Tree viewNamesTree, String candidate, String origin, boolean searchForOrigin) {
            if (searchForOrigin) {
                if (viewNamesTree.name.equals(origin)) {
                    return true;
                }
            } else {
                if (viewNamesTree.name.equals(candidate)) {
                    return traverseOptimalType(viewNamesTree, candidate, origin, true);
                }
            }

            for (Tree child : viewNamesTree.children) {
                if (traverseOptimalType(child, candidate, origin, searchForOrigin)) {
                    return true;
                }
            }

            return false;
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

    private static class Tree {
        public String name;
        public List<Tree> children; // Set is enough

        private Tree(String name) {
            this.name = name;
            children = Lists.newArrayList();
        }
    }

    public void execute(Project project, VirtualFile file, ConvertConfig config) {
        List<AndroidViewInfo> infos;
        InputStream is = null;
        try {
            is = file.getInputStream();
            infos = extractViewInfos(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Util.closeQuietly(is);
        }

        Tree viewNameTree = config.useSmartType ? prepareViewNames(project) : null;
        String javaCode = generateJavaCode(infos, viewNameTree, config);

        CopyPasteManager.getInstance().setContents(new StringSelection(javaCode));

        // add layout file name to message
        Notifications.Bus.notify(new Notification("OffingHarbor", "OffingHarbor", "Code is copied to clipboard", NotificationType.INFORMATION), project);
    }

    private List<AndroidViewInfo> extractViewInfos(InputStream is) {
        try {
            return traverseViewInfos(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is));
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

    private String generateJavaCode(List<AndroidViewInfo> infos, Tree viewNameTree, ConvertConfig config) {
        StringBuilder fieldJavaCode = new StringBuilder();
        StringBuilder methodJavaCode = new StringBuilder();

        String NL = "\n";


        final String visibility;
        switch (config.visibility) {
            case PROTECTED:
                visibility = "protected ";
                break;
            case PACKAGE_PRIVATE:
                visibility = "";
                break;
            case PRIVATE:
                visibility = "private ";
                break;
            default:
                throw new IllegalStateException("assert");
        }

        methodJavaCode.append("private void assignViews() {" + NL);

        for (AndroidViewInfo info : infos) {
            String type = config.useSmartType ? info.findOptimalType(viewNameTree) : info.type;
            String symbol = info.getJavaSymbolName(config);

            if (config.format == ConvertConfig.ConvertFormat.ANDROID_ANNOTATIONS) {
                if (config.prefix.willModify()) {
                    fieldJavaCode.append(String.format("@ViewById(R.id.%s)" + NL + "%s%s %s;" + NL, info.id, visibility, type, symbol));
                } else {
                    fieldJavaCode.append(String.format("@ViewById" + NL + "%s%s %s;" + NL, visibility, type, symbol));
                }
            } else if (config.format == ConvertConfig.ConvertFormat.BUTTER_KNIFE) {
                // Butter Knife always requires resource-id
                fieldJavaCode.append(String.format("@InjectView(R.id.%s)" + NL + "%s%s %s;" + NL, info.id, visibility, type, symbol));
            } else {
                fieldJavaCode.append(String.format("%s%s %s;" + NL, visibility, type, symbol));
            }

            if (type.equals("View")) {
                methodJavaCode.append(String.format("    %s = findViewById(R.id.%s);" + NL, symbol, info.id));
            } else {
                methodJavaCode.append(String.format("    %s = (%s) findViewById(R.id.%s);" + NL, symbol, type, info.id));
            }
        }

        methodJavaCode.append("}" + NL);

        if (config.format.requireAssignMethod()) {
            return fieldJavaCode.toString() + NL + methodJavaCode.toString();
        } else {
            return fieldJavaCode.toString();
        }
    }

    private Tree prepareViewNames(Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        return traverseViewNames(JavaPsiFacade.getInstance(project).findClass("android.view.View", scope), scope);
    }

    private Tree traverseViewNames(PsiClass psiClass, SearchScope scope) {
        Tree tree = new Tree(psiClass.getName());

        for (PsiClass child : ClassInheritorsSearch.search(psiClass, psiClass.getUseScope().intersectWith(scope), false).findAll()) {
            tree.children.add(traverseViewNames(child, scope));
        }

        return tree;
    }
}
