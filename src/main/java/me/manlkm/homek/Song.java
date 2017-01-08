/**
 * 
 */
package me.manlkm.homek;

/**
 * @author manlkm
 *
 */
public class Song {
	private String poster;
	private SongSource sources;
	
	public String getPoster() {
		return poster;
	}
	public void setPoster(String poster) {
		this.poster = poster;
	}
	public SongSource getSources() {
		return sources;
	}
	public void setSources(SongSource sources) {
		this.sources = sources;
	}
	
	@Override
	public String toString() {
		return "Song [poster=" + poster + ", sources=" + sources + "]";
	}
	
}
