public class ConvertConfig {
    public enum ConvertPrefix {
        NONE, MEMBER, UNDERSCORE;

        public boolean willModify() {
            return this != NONE;
        }
    }

    public enum ConvertFormat {
        PLAIN, ANDROID_ANNOTATIONS, BUTTER_KNIFE
    }

    public enum Visibility {
        PRIVATE, PACKAGE_PRIVATE, PROTECTED
    }

    public ConvertPrefix prefix;
    public ConvertFormat format;
    public Visibility visibility;
    public boolean useSmartType;

    public ConvertConfig() {
        // default values

        prefix = ConvertPrefix.NONE;
        format = ConvertFormat.PLAIN;
        visibility = Visibility.PRIVATE;
        useSmartType = false;
    }
}

