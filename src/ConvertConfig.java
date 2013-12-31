public class ConvertConfig {
    public enum ConvertPrefix {
        NONE, MEMBER, UNDERSCORE
    }

    public enum ConvertFormat {
        PLAIN, ANDROID_ANNOTATIONS
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

