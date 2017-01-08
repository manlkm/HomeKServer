/**
 * 
 */
package me.manlkm.homek;

/**
 * @author manlkm
 *
 */
public class MusicVideo {
	private String id;
	private MusicVideoCat cat;
	private String singer;
	private String songName;
	private String fileType;
	private String fullFileName;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public MusicVideoCat getCat() {
		return cat;
	}
	public void setCat(MusicVideoCat cat) {
		this.cat = cat;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	public String getSongName() {
		return songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public String getFullFileName() {
		return fullFileName;
	}
	public void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}
	
	@Override
	public String toString() {
		return "MusicVideo [id=" + id + ", cat=" + cat + ", singer=" + singer + ", songName=" + songName + ", fileType="
				+ fileType + ", fullFileName=" + fullFileName + "]";
	}

}
