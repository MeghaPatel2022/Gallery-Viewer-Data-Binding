package gallery.photoapp.gallerypro.photoviewer.model;

import java.io.Serializable;

public class ImagesModel implements Serializable {

    int id;
    String path;
    String name;
    String date;
    long dateTaken;
    long dateModified;
    String imageHeightWidth;
    String pictureSize;

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getImageHeightWidth() {
        return imageHeightWidth;
    }

    public void setImageHeightWidth(String imageHeightWidth) {
        this.imageHeightWidth = imageHeightWidth;
    }

    public String getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(String pictureSize) {
        this.pictureSize = pictureSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
