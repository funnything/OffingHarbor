import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by toyama.yosaku on 13/12/30.
 */
public class ConvertDialog extends DialogWrapper {
    protected ConvertDialog(@Nullable Project project) {
        super(project, true);

        setTitle("Convert Layout XML to Java");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        Box box = Box.createHorizontalBox();
//        box.setAlignmentY(Component.TOP_ALIGNMENT);

        // create prefix

        Box prefixBox = Box.createVerticalBox();
        prefixBox.setAlignmentY(Component.TOP_ALIGNMENT);
        prefixBox.setBorder(IdeBorderFactory.createTitledBorder("Field Name Prefix", true));

        JRadioButton mPrefixAsIsRadioButton = new JRadioButton("As is (None)");
        JRadioButton mPrefixMemberRadioButton = new JRadioButton("m");
        JRadioButton mPrefixUnderscoreRadioButton = new JRadioButton("_ (underscore)");

        ButtonGroup prefixButtonGroup = new ButtonGroup();
        prefixButtonGroup.add(mPrefixAsIsRadioButton);
        prefixButtonGroup.add(mPrefixMemberRadioButton);
        prefixButtonGroup.add(mPrefixUnderscoreRadioButton);

        prefixBox.add(mPrefixAsIsRadioButton);
        prefixBox.add(mPrefixMemberRadioButton);
        prefixBox.add(mPrefixUnderscoreRadioButton);

        box.add(prefixBox);

        // create format

        Box formatBox = Box.createVerticalBox();
        formatBox.setAlignmentY(Component.TOP_ALIGNMENT);
        formatBox.setBorder(IdeBorderFactory.createTitledBorder("Conversion Format", true));

        JRadioButton mFormatPlainRadioButton = new JRadioButton("for plain Android frameworks");
        JRadioButton mFormatAndroidAnnotationsRadioButton = new JRadioButton("for AndroidAnnotations");

        ButtonGroup formatButtonGroup = new ButtonGroup();
        formatButtonGroup.add(mFormatPlainRadioButton);
        formatButtonGroup.add(mFormatAndroidAnnotationsRadioButton);

        formatBox.add(mFormatPlainRadioButton);
        formatBox.add(mFormatAndroidAnnotationsRadioButton);

        box.add(Box.createHorizontalStrut(5));
        box.add(formatBox);

        // create smart-type

        Box smartTypeBox = Box.createVerticalBox();
        smartTypeBox.setAlignmentY(Component.TOP_ALIGNMENT);
        smartTypeBox.setBorder(IdeBorderFactory.createTitledBorder("Smart type detection", true));

        JCheckBox mSmartTypeCheckBox = new JCheckBox("Detect Type by ID");
        smartTypeBox.add(mSmartTypeCheckBox);

        box.add(Box.createHorizontalStrut(5));
        box.add(smartTypeBox);

//        Box b = Box.createVerticalBox();
//        b.setBorder(IdeBorderFactory.createTitledBorder("Prefix", true));
//        JPanel fieldsVisibilityPanel = new JPanel(new BorderLayout());
//        fieldsVisibilityPanel.setBorder(IdeBorderFactory.createTitledBorder(
//                RefactoringBundle.message("encapsulate.fields..encapsulated.fields.visibility.border.title"), true));
//        fieldsVisibilityPanel.add(prefixBox, BorderLayout.CENTER);
//        fieldsVisibilityPanel.add(Box.createHorizontalStrut(5), BorderLayout.WEST);

//        return pane;
        return box;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
