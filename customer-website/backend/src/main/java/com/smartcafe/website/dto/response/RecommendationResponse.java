package com.smartcafe.website.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse {

    private boolean success;
    private String  context;
    private List<RecommendedItem> recommendations;
    private int     total;
    private String  generatedAt;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendedItem {
        private int    id;
        private String name;
        private String category;
        private double price;
        private String imageUrl;
        private String reason;
        private double confidence;
        private String algorithm;

        public int    getId()          { return id; }
        public void   setId(int v)     { id = v; }
        public String getName()        { return name; }
        public void   setName(String v){ name = v; }
        public String getCategory()    { return category; }
        public void   setCategory(String v){ category = v; }
        public double getPrice()       { return price; }
        public void   setPrice(double v){ price = v; }
        public String getImageUrl()    { return imageUrl; }
        public void   setImageUrl(String v){ imageUrl = v; }
        public String getReason()      { return reason; }
        public void   setReason(String v){ reason = v; }
        public double getConfidence()  { return confidence; }
        public void   setConfidence(double v){ confidence = v; }
        public String getAlgorithm()   { return algorithm; }
        public void   setAlgorithm(String v){ algorithm = v; }
    }

    public boolean              isSuccess()                   { return success; }
    public void                 setSuccess(boolean v)         { success = v; }
    public String               getContext()                  { return context; }
    public void                 setContext(String v)          { context = v; }
    public List<RecommendedItem>getRecommendations()          { return recommendations; }
    public void                 setRecommendations(List<RecommendedItem> v){ recommendations = v; }
    public int                  getTotal()                    { return total; }
    public void                 setTotal(int v)               { total = v; }
    public String               getGeneratedAt()              { return generatedAt; }
    public void                 setGeneratedAt(String v)      { generatedAt = v; }
}
