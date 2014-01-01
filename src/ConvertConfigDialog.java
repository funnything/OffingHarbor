import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by toyama.yosaku on 13/12/30.
 */
public class ConvertConfigDialog extends DialogWrapper {
    private AnActionEvent mSourceEvent;
    private ConvertConfigRepository mRepository;
    //
    private JRadioButton mPrefixAsIsRadioButton;
    private JRadioButton mPrefixMemberRadioButton;
    private JRadioButton mPrefixUnderscoreRadioButton;
    private JRadioButton mFormatPlainRadioButton;
    private JRadioButton mFormatAndroidAnnotationsRadioButton;
    private JCheckBox mSmartTypeCheckBox;

    public ConvertConfigDialog(AnActionEvent sourceEvent) {
        super(sourceEvent.getProject(), true);

        mSourceEvent = sourceEvent;

        mRepository = ConvertConfigRepository.getInstance(sourceEvent.getProject());
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

        mSmartTypeCheckBox = new JCheckBox("Detect Type by ID");
        smartTypeBox.add(mSmartTypeCheckBox);

        box.add(Box.createHorizontalStrut(5));
        box.add(smartTypeBox);

        setConfig(mRepository.getState());

        return box;
    }

    private ConvertConfig getConfig() {
        ConvertConfig config = new ConvertConfig();
        config.prefix = getPrefix();
        config.format = getFormat();
        config.useSmartType = mSmartTypeCheckBox.isSelected();
        return config;
    }

    private void setConfig(ConvertConfig config) {
        setPrefix(config.prefix);
        setFormat(config.format);
        mSmartTypeCheckBox.setSelected(config.useSmartType);
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
            default:
                throw new IllegalStateException("assert");
        }
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        ConvertConfig config = getConfig();

        mRepository.loadState(config);
        new ConvertExecutor().execute(mSourceEvent, config);
    }
}
