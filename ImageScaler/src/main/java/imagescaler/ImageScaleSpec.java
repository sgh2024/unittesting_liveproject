package imagescaler;

public class ImageScaleSpec {
    private Integer targetWidth;
    private Integer targetHeight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageScaleSpec that = (ImageScaleSpec) o;

        if (getPreserveAspectRatio() != that.getPreserveAspectRatio()) return false;
        if (getTargetWidth() != null ? !getTargetWidth().equals(that.getTargetWidth()) : that.getTargetWidth() != null)
            return false;
        return getTargetHeight() != null ? getTargetHeight().equals(that.getTargetHeight()) : that.getTargetHeight() == null;
    }

    @Override
    public int hashCode() {
        int result = getTargetWidth() != null ? getTargetWidth().hashCode() : 0;
        result = 31 * result + (getTargetHeight() != null ? getTargetHeight().hashCode() : 0);
        result = 31 * result + (getPreserveAspectRatio() ? 1 : 0);
        return result;
    }

    private boolean preserveAspectRatio;

    public void setTargetWidth(Integer value) {
        targetWidth = value;
    }

    public Integer getTargetWidth() {
        return targetWidth;
    }

    public void setTargetHeight(Integer value) {
        targetHeight = value;
    }

    public Integer getTargetHeight() {
        return targetHeight;
    }

    public boolean getPreserveAspectRatio() {
        return preserveAspectRatio;
    }
}
