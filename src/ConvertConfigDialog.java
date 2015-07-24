import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by toyama.yosaku on 13/12/30.
 */
public class ConvertConfigDialog extends DialogWrapper {
    private Project mProject;
    private VirtualFile mLayoutFile;
    private ConvertConfigRepository mRepository;
    //
    private JRadioButton mPrefixAsIsRadioButton;
    private JRadioButton mPrefixMemberRadioButton;
    private JRadioButton mPrefixUnderscoreRadioButton;
    private JRadioButton mFormatPlainRadioButton;
    private JRadioButton mFormatAndroidAnnotationsRadioButton;
    private JRadioButton mFormatButterKnifeRadioButton;
    private JRadioButton mVisibilityPrivate;
    private JRadioButton mVisibilityPackagePrivate;
    private JRadioButton mVisibilityProtected;
    private JCheckBox mSmartTypeCheckBox;
    private JCheckBox mIncludeNodeCheckBox;

    public ConvertConfigDialog(Project project, VirtualFile layoutFile) {
        super(project, true);

        mProject = project;
        mLayoutFile = layoutFile;

        mRepository = ConvertConfigRepository.getInstance(project);
        assert mRepository != null;

        if (mRepository.getState() == null) {
            mRepository.loadState(new ConvertConfig());
        }

        setTitle("Convert Layout XML to Java");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        Box box = Box.createHorizontalBox();

        // create prefix

        Box prefixBox = Box.createVerticalBox();
        prefixBox.setAlignmentY(Component.TOP_ALIGNMENT);
        prefixBox.setBorder(IdeBorderFactory.createTitledBorder("Field Name Prefix", true));

        mPrefixAsIsRadioButton = new JRadioButton("As is (None)");
        mPrefixMemberRadioButton = new JRadioButton("m");
        mPrefixUnderscoreRadioButton = new JRadioButton("_ (underscore)");

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

        mFormatPlainRadioButton = new JRadioButton("for plain Android frameworks");
        mFormatAndroidAnnotationsRadioButton = new JRadioButton("for AndroidAnnotations");
        mFormatButterKnifeRadioButton = new JRadioButton("for ButterKnife");

        ActionListener formatChangeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getSource() == mFormatPlainRadioButton) {
                    applyVisibilityConstraint(ConvertConfig.ConvertFormat.PLAIN);
                } else if (actionEvent.getSource() == mFormatAndroidAnnotationsRadioButton) {
                    applyVisibilityConstraint(ConvertConfig.ConvertFormat.ANDROID_ANNOTATIONS);
                } else if (actionEvent.getSource() == mFormatButterKnifeRadioButton) {
                    applyVisibilityConstraint(ConvertConfig.ConvertFormat.BUTTER_KNIFE);
                }
            }
        };

        mFormatPlainRadioButton.addActionListener(formatChangeListener);
        mFormatAndroidAnnotationsRadioButton.addActionListener(formatChangeListener);
        mFormatButterKnifeRadioButton.addActionListener(formatChangeListener);

        ButtonGroup formatButtonGroup = new ButtonGroup();
        formatButtonGroup.add(mFormatPlainRadioButton);
        formatButtonGroup.add(mFormatAndroidAnnotationsRadioButton);
        formatButtonGroup.add(mFormatButterKnifeRadioButton);

        formatBox.add(mFormatPlainRadioButton);
        formatBox.add(mFormatAndroidAnnotationsRadioButton);
        formatBox.add(mFormatButterKnifeRadioButton);

        box.add(Box.createHorizontalStrut(5));
        box.add(formatBox);

        // create visibility

        Box visibilityBox = Box.createVerticalBox();
        visibilityBox.setAlignmentY(Component.TOP_ALIGNMENT);
        visibilityBox.setBorder(IdeBorderFactory.createTitledBorder("Variable Visibility", true));

        mVisibilityPrivate = new JRadioButton("private");
        mVisibilityPackagePrivate = new JRadioButton("package private");
        mVisibilityProtected = new JRadioButton("protected");

        ButtonGroup visibilityButtonGroup = new ButtonGroup();
        visibilityButtonGroup.add(mVisibilityPrivate);
        visibilityButtonGroup.add(mVisibilityPackagePrivate);
        visibilityButtonGroup.add(mVisibilityProtected);

        visibilityBox.add(mVisibilityPrivate);
        visibilityBox.add(mVisibilityPackagePrivate);
        visibilityBox.add(mVisibilityProtected);

        box.add(visibilityBox);

        // create smart-type

        Box smartTypeBox = Box.createVerticalBox();
        smartTypeBox.setAlignmentY(Component.TOP_ALIGNMENT);
        smartTypeBox.setBorder(IdeBorderFactory.createTitledBorder("Smart type detection", true));

        mSmartTypeCheckBox = new JCheckBox("Detect Type by ID");
        smartTypeBox.add(mSmartTypeCheckBox);

        mIncludeNodeCheckBox = new JCheckBox("Detect 'include' tag");
        mIncludeNodeCheckBox.setSelected(true);
        smartTypeBox.add(mIncludeNodeCheckBox);

        box.add(Box.createHorizontalStrut(5));
        box.add(smartTypeBox);

        setConfig(mRepository.getState());

        return box;
    }

    private ConvertConfig getConfig() {
        ConvertConfig config = new ConvertConfig();
        config.prefix = getPrefix();
        config.format = getFormat();
        config.visibility = getVisibility();
        config.useSmartType = mSmartTypeCheckBox.isSelected();
        config.detectIncludeNode = mIncludeNodeCheckBox.isSelected();
        return config;
    }

    private void setConfig(ConvertConfig config) {
        setPrefix(config.prefix);
        setFormat(config.format);
        setVisibility(config.visibility);
        mSmartTypeCheckBox.setSelected(config.useSmartType);

        applyVisibilityConstraint(config.format);
    }

    private ConvertConfig.ConvertPrefix getPrefix() {
        if (mPrefixAsIsRadioButton.isSelected()) {
            return ConvertConfig.ConvertPrefix.NONE;
        } else if (mPrefixMemberRadioButton.isSelected()) {
            return ConvertConfig.ConvertPrefix.MEMBER;
        } else if (mPrefixUnderscoreRadioButton.isSelected()) {
            return ConvertConfig.ConvertPrefix.UNDERSCORE;
        } else {
            throw new IllegalStateException("assert");
        }
    }

    private void setPrefix(ConvertConfig.ConvertPrefix prefix) {
        switch (prefix) {
            case NONE:
                mPrefixAsIsRadioButton.setSelected(true);
                break;
            case MEMBER:
                mPrefixMemberRadioButton.setSelected(true);
                break;
            case UNDERSCORE:
                mPrefixUnderscoreRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalStateException("assert");
        }
    }

    private ConvertConfig.ConvertFormat getFormat() {
        if (mFormatPlainRadioButton.isSelected()) {
            return ConvertConfig.ConvertFormat.PLAIN;
        } else if (mFormatAndroidAnnotationsRadioButton.isSelected()) {
            return ConvertConfig.ConvertFormat.ANDROID_ANNOTATIONS;
        } else if (mFormatButterKnifeRadioButton.isSelected()) {
            return ConvertConfig.ConvertFormat.BUTTER_KNIFE;
        } else {
            throw new IllegalStateException("assert");
        }
    }

    private void setFormat(ConvertConfig.ConvertFormat format) {
        switch (format) {
            case PLAIN:
                mFormatPlainRadioButton.setSelected(true);
                break;
            case ANDROID_ANNOTATIONS:
                mFormatAndroidAnnotationsRadioButton.setSelected(true);
                break;
            case BUTTER_KNIFE:
                mFormatButterKnifeRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalStateException("assert");
        }
    }

    private void applyVisibilityConstraint(ConvertConfig.ConvertFormat format) {
        switch (format) {
            case PLAIN:
                mVisibilityPrivate.setEnabled(true);
                mVisibilityPackagePrivate.setEnabled(true);
                mVisibilityProtected.setEnabled(true);
                break;
            case ANDROID_ANNOTATIONS:
                /* fall through */
            case BUTTER_KNIFE:
                mVisibilityPrivate.setEnabled(false);
                mVisibilityPackagePrivate.setEnabled(true);
                mVisibilityProtected.setEnabled(true);

                // PLAIN でない時に許される可視性は private 以外
                if (getVisibility() == ConvertConfig.Visibility.PRIVATE) {
                    setVisibility(ConvertConfig.Visibility.PACKAGE_PRIVATE);
                }
                break;
            default:
                throw new IllegalStateException("assert");
        }
    }

    private ConvertConfig.Visibility getVisibility() {
        if (mVisibilityPrivate.isSelected()) {
            return ConvertConfig.Visibility.PRIVATE;
        } else if (mVisibilityPackagePrivate.isSelected()) {
            return ConvertConfig.Visibility.PACKAGE_PRIVATE;
        } else if (mVisibilityProtected.isSelected()) {
            return ConvertConfig.Visibility.PROTECTED;
        } else {
            throw new IllegalStateException("assert");
        }
    }

    private void setVisibility(ConvertConfig.Visibility visibility) {
        switch (visibility) {
            case PRIVATE:
                mVisibilityPrivate.setSelected(true);
                break;
            case PACKAGE_PRIVATE:
                mVisibilityPackagePrivate.setSelected(true);
                break;
            case PROTECTED:
                mVisibilityProtected.setSelected(true);
                break;
            default:
                throw new IllegalStateException("assert");
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        ConvertConfig config = getConfig();

        mRepository.loadState(config);
        new ConvertExecutor().execute(mProject, mLayoutFile, config);
    }
}
