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

    public ConvertPrefix prefix;
    public ConvertFormat format;
    public boolean useSmartType;

    public ConvertConfig() {
        // default values

        prefix = ConvertPrefix.NONE;
        format = ConvertFormat.PLAIN;
        useSmartType = false;
    }
}

