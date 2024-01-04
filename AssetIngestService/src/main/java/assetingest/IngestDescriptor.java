package assetingest;

public class IngestDescriptor {
    private String fullResolutionUrl;
    private TextDescriptor text = new TextDescriptor();
    private PricingDescriptor pricing = new PricingDescriptor();

    public void setFullResolutionUrl(String fullResolutionUrl) {
        this.fullResolutionUrl = fullResolutionUrl;
    }

    public String getFullResolutionUrl() {
        return fullResolutionUrl;
    }

    public TextDescriptor getText() {
        if (text == null)
            text = new TextDescriptor();

        return text;
    }

    public void setText(TextDescriptor value) {
        this.text = value;
    }

    public PricingDescriptor getPricing() {
        if (pricing == null)
            pricing = new PricingDescriptor();

        return pricing;
    }

    public void setPricing(PricingDescriptor pricing) {
        this.pricing = pricing;
    }

    public static class TextDescriptor {
        private LanguageTextDescriptor english;
        private LanguageTextDescriptor german;
        private LanguageTextDescriptor spanish;
        private LanguageTextDescriptor french;

        public LanguageTextDescriptor getEnglish() {
            return english;
        }

        public void setEnglish(LanguageTextDescriptor value) {
            this.english = value;
        }

        public void setGerman(LanguageTextDescriptor value) {
            this.german = value;
        }

        public LanguageTextDescriptor getGerman() {
            return german;
        }

        public void setSpanish(LanguageTextDescriptor value) {
            this.spanish = value;
        }

        public LanguageTextDescriptor getSpanish() {
            return spanish;
        }

        public void setFrench(LanguageTextDescriptor value) {
            this.french = value;
        }

        public LanguageTextDescriptor getFrench() {
            return french;
        }
    }

    public static class PricingDescriptor {
        private String initialPricingCode;

        public void setInitialPricingCode(String initialPricingCode) {
            this.initialPricingCode = initialPricingCode;
        }

        public String getInitialPricingCode() {
            return initialPricingCode;
        }
    }

    public static class LanguageTextDescriptor {
        private String title;
        private String description;

        public void setTitle(String value) {
            this.title = value;
        }

        public String getTitle() {
            return title;
        }

        public void setDescription(String value) {
            this.description = value;
        }

        public String getDescription() {
            return description;
        }
    }
}
