package amrutapani.location_aware;

/**
 * Created by Amruta Pani on 05-10-2016.
 */

public class VisitRecord {
    private String coName, villageName, address, visitDate;
    private double latitude, longitude;

    private int recordId;

    public VisitRecord(String coName, String villageName, double latitude, double longitude, String address, String visitDate) {
        setCoName(coName);
        setVillageName(villageName);
        setLatitude(latitude);
        setLongitude(longitude);
        setAddress(address);
        setVisitDate(visitDate);
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getRecordId() {
        return recordId;
    }

    public String getCoName() {
        return coName;
    }

    public String getVillageName() {
        return villageName;
    }

    public String getAddress() {
        return address;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setCoName(String coName) {
        this.coName = coName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
