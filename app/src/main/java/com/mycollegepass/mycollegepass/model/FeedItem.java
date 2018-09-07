package com.mycollegepass.mycollegepass.model;

/***The model describes the features of a single feed item
 * For example: a feed may be
 *      discountID = 34234211423
 *      business_name = "Molly's"
 *      redemption_alert = 10
 *      description = "get 10% off all meals on our regular menu"
 *      image = [ an image set by the business,descriptive of the product offered]
 *
 *
 ***/
public class FeedItem {

    private String discountID;
    private String business_name;
    private float discount;
    private String description;
    private String image_url;
    private double latitude;
    private double longitude;
    private String type;
    private String website;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    private long rank;


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }




    private String terms;
    private String redeemCode;



    private int numOfLikes;




    /*
    private String Terms_and_Conditions;
    private Date time_limit;
    */

    public FeedItem(){
        this.business_name = "";
        this.discount = 0;
        this.description = "";
        this.image_url = "";
    }


    public String getDiscountID() {
        return discountID;
    }

    public void setDiscountID(String discountID) {
        this.discountID = discountID;
    }
    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getNumOfLikes() {
        return numOfLikes;
    }

    public void setNumOfLikes(int numOfLikes) {
        this.numOfLikes = numOfLikes;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getRedeemCode() {
        return redeemCode;
    }

    public void setRedeemCode(String redeemCode) {
        this.redeemCode = redeemCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
