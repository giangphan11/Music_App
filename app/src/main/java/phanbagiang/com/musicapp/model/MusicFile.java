package phanbagiang.com.musicapp.model;

public class MusicFile {
    private String path;
    private String title;
    private String duration;
    private String album;
    private String artist;

    public MusicFile() {
    }

    public MusicFile(String path, String title, String duration, String album, String artist) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.album = album;
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
