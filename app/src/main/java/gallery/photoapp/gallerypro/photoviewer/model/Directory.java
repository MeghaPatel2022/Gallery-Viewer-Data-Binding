package gallery.photoapp.gallerypro.photoviewer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent Woo
 * Date: 2016/10/10
 * Time: 17:31
 */

public class Directory<T> implements Comparable {
    private String id;
    private String name;
    private String path;
    private boolean isDirectory = false;
    private List<T> files = new ArrayList<>();

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getFiles() {
        return files;
    }

    public void setFiles(List<T> files) {
        this.files = files;
    }

    public void addFile(T file) {
        files.add(file);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Directory)) return false;

        Directory directory = (Directory) o;
        return this.name.equals(directory.name);
    }


    @Override
    public int compareTo(Object o) {
        return (Integer.compare(getFiles().size(), ((Directory) o).getFiles().size()));
    }

    @Override
    public int hashCode() {

        return path.hashCode();
    }
}
