package plugins.lychee;

public class LycheePicture implements Comparable<LycheePicture>{
	private String id = "";
	private String title = "";
	private String tags = "";
	private boolean isPublic = false;
	private boolean star = false;
	private String album = "";
	private String thumbUrl = "";
	private long takeStamp = 0;
	private String url = "";
	private String sysDate = "";
	private String previousPhoto = "";
	private String nextPhoto = "";
	private long cameraDate = 0;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public boolean isStar() {
		return star;
	}
	public void setStar(boolean star) {
		this.star = star;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getThumbUrl() {
		return thumbUrl;
	}
	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}
	public long getTakeStamp() {
		return takeStamp;
	}
	public void setTakeStamp(long takeStamp) {
		this.takeStamp = takeStamp;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSysDate() {
		return sysDate;
	}
	public void setSysDate(String sysDate) {
		this.sysDate = sysDate;
	}
	public String getPreviousPhoto() {
		return previousPhoto;
	}
	public void setPreviousPhoto(String previousPhoto) {
		this.previousPhoto = previousPhoto;
	}
	public String getNextPhoto() {
		return nextPhoto;
	}
	public void setNextPhoto(String nextPhoto) {
		this.nextPhoto = nextPhoto;
	}
	public long getCameraDate() {
		return cameraDate;
	}
	public void setCameraDate(long cameraDate) {
		this.cameraDate = cameraDate;
	}
	@Override
	public int compareTo(LycheePicture o) {
		try{
			return Long.compare(Long.parseLong(id), Long.parseLong(o.id));
		}catch(Exception e){
			return id.compareTo(o.id);
		}
	}
	
	
}
