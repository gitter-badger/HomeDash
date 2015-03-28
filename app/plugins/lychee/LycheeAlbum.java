package plugins.lychee;

import java.util.ArrayList;
import java.util.List;

public class LycheeAlbum implements Comparable<LycheeAlbum>{
	private String id = "";
	private String title = "";
	private long sysstamp = 0;
	private boolean password = false;
	private String thumb0;
	private String thumb1;
	private String thumb2;
	private boolean isPublic = false;
	private String description = "";
	private String sysDate = "";
	private boolean visible = true;
	private boolean downloadable = false;
	private int num = 0;
	private List<LycheePicture> pictures = new ArrayList<>();

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

	public long getSysstamp() {
		return sysstamp;
	}

	public void setSysstamp(long sysstamp) {
		this.sysstamp = sysstamp;
	}

	public String getThumb0() {
		return thumb0;
	}

	public void setThumb0(String thumb0) {
		this.thumb0 = thumb0;
	}

	public String getThumb1() {
		return thumb1;
	}

	public void setThumb1(String thumb1) {
		this.thumb1 = thumb1;
	}

	public String getThumb2() {
		return thumb2;
	}

	public void setThumb2(String thumb2) {
		this.thumb2 = thumb2;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isDownloadable() {
		return downloadable;
	}

	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}

	public List<LycheePicture> getPictures() {
		return pictures;
	}

	public void setPictures(List<LycheePicture> pictures) {
		this.pictures = pictures;
	}

	public boolean getPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}

	public String getSysDate() {
		return sysDate;
	}

	public void setSysDate(String sysDate) {
		this.sysDate = sysDate;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public int compareTo(LycheeAlbum o) {
		
		return id.compareTo(o.id);
	}
	
	

}
