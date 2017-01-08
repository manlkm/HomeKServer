/**
 * 
 */
package me.manlkm.homek;

/**
 * @author manlkm
 *
 */
public enum MusicVideoCat {
	MALE("M"),
    FEMALE("F"),
    TEAM("T");
 
    private String value;
 
    private MusicVideoCat(String value) {
        this.value = value;
    }
 
    public String getValue() {
        return this.value;
    }
    
    public static MusicVideoCat findByValue(String value){
    	for(int i=0; i<MusicVideoCat.values().length; i++){
    		if(value.equalsIgnoreCase(MusicVideoCat.values()[i].getValue())){
    			return MusicVideoCat.values()[i];
    		}
    	}
    	
    	return null;
    	
    }
}
