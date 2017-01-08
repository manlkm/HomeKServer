/**
 * 
 */
package me.manlkm.homek;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.options;
import static spark.Spark.delete;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.utils.StringUtils;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;


/**
 * @author manlkm
 *
 */
public class HomeKStarter {
	private static Logger logger = LoggerFactory.getLogger(HomeKStarter.class);
	private static final HashMap<String, String> corsHeaders = new HashMap<String, String>();
	private static final HashMap<String, List<String>> userCommandMap = new HashMap<String, List<String>>();
	private static final HashMap<String, LinkedList<String>> userOrderedSongMap = new HashMap<String, LinkedList<String>>();
    //private static LinkedList<Song> orderedSongs = new LinkedList<Song>();
    private static List<MusicVideo> musicVideos = null;
    private static String musicVideoScanDir = null;
    private static String musicVideoFileRoot = null;
    
    static {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        corsHeaders.put("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }

    public final static void apply() {
        Filter filter = new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                corsHeaders.forEach((key, value) -> {
                    response.header(key, value);
                });
            }
        };
        Spark.after(filter);
    }
    
    private static Song convertMusicVideoToSong(MusicVideo mv){
	    SongSource sources1 = new SongSource();
	    sources1.setSrc(FilenameUtils.concat(musicVideoFileRoot, mv.getFullFileName()));
	    sources1.setType(mv.getFileType());
	    Song song1 = new Song();
	    song1.setSources(sources1);
	    
	    return song1;
    }
    
    private static Song getCurrentSong(String uid){
    	LinkedList<String> orderedSongs = userOrderedSongMap.get(uid);
    	if(orderedSongs != null && orderedSongs.size() > 0){
    		//orderedSongs.removeFirst();
    		
    		if(orderedSongs.size()>0){
    			logger.info("song returned: {}", orderedSongs.getFirst());
    			try {
					MusicVideo mv = findMusicVideoById(orderedSongs.getFirst());
					if(mv != null){
						return convertMusicVideoToSong(mv);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}
    	
    	logger.debug("No songs in the list");
    	return null;
    }
    
    private static MusicVideo findMusicVideoById(String id) throws Exception{
    	if(!StringUtils.isEmpty(id)){
    		for(MusicVideo mv : musicVideos){
    			if(mv.getId().equals(id)){
    				return mv;
    			}
    		}
    	}
    	
    	throw new Exception("MusicVideo record could not be found for id="+id);
    }
    
    private static void initConfig(){
    	final Properties properties = new Properties();
		try (final InputStream stream = HomeKStarter.class.getClassLoader().getResourceAsStream("config.properties")){
			properties.load(stream);
			musicVideoScanDir = (String) properties.get("scan.dir");
			musicVideoFileRoot = (String) properties.get("mv.root.dir");
			
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HomeKStarter.apply();
		
		initConfig();
		
		try {
			musicVideos = MusicVideoScanner.buildMusicVideoList(musicVideoScanDir);
			
			if(musicVideos != null){
				logger.info("loading music video records");
				for(MusicVideo mv : musicVideos){
					logger.info(mv.toString());
					
				}
				logger.info("loading music video records completed");
			}
			else{	
				logger.info("no music video records loaded");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		post("/login", (request, response) -> {
		    return processLogin(request, response);
		});
		
		options("/command", (request, response) -> {
		    return "";
		});
		
		options("/song", (request, response) -> {
		    return "";
		});
		
		options("/song/:uid", (request, response) -> {
		    return "";
		});
		
		post("/command", (request, response) -> {
		    return processAddCommand(request, response);
		});
		
		put("/command", (request, response) -> {
		    return processUpdateCommand(request, response);
		});
		
		get("/command/:uid", (request, response) -> {
		    return processGetCommand(request, response);
		});

		get("/song/:uid", (request, response) -> {
		    return processGetSelectedSong(request, response);
		});
		
		post("/song", (request, response) -> {
		    return processAddSong(request, response);
		});
		
		delete("/song/:uid", (request, response) -> {
		    return processRemoveFirstSong(request, response);
		});
		
		get("/next/:uid", (request, response) -> {
		    return processNext(request, response);
		});
		
		get("/singer/cat/:cat", (request, response) -> {
		    return processGetSinger(request, response);
		});
		
		get("/song/singer/:singer", (request, response) -> {
		    return processGetSongBySinger(request, response);
		});
	}
	
	private static String processLogin(Request request, Response response){
		if(request.params("uid") != null){
			request.session().attribute("uid", request.params("uid"));
			
			return "{ \"result\": \"success\" }";
		}
		
		return "{ \"result\": \"failed\" }";
	}
	
	private static String processGetSinger(Request request, Response response){
		try{
			String catStr = request.params(":cat");
			MusicVideoCat cat = MusicVideoCat.findByValue(catStr);
			
			logger.info("cat: [{}]", cat);
			
			if(cat != null){
				LinkedList<String> singers = new LinkedList<String>();
				if(musicVideos!=null && musicVideos.size()>0){
					for(MusicVideo mv : musicVideos){
						if(mv.getCat() == cat){
							singers.add(mv.getSinger());
						}
					}
					
					return getGsonInstance().toJson(singers);
				}
				
			}
			else{
				logger.error("cat is invalid");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "{}";
	}
	
	private static String processGetSongBySinger(Request request, Response response){
		try{
			String singer = request.params(":singer");
			
			logger.info("processGetSongBySinger | singer: [{}]", singer);
			
			if(!StringUtils.isEmpty(singer)){
				LinkedList<MusicVideo> mvList = new LinkedList<MusicVideo>();
				if(musicVideos!=null && musicVideos.size()>0){
					for(MusicVideo mv : musicVideos){
						if(mv.getSinger().equals(singer)){
							mvList.add(mv);
						}
					}
					
					return getGsonInstance().toJson(mvList);
				}
				
			}
			else{
				logger.error("singer is invalid");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "{}";
	
	}
	
	private static String processGetCommand(Request request, Response response){
		//List<String> commands = userCommandMap.get((String)(request.session().attribute("uid")));
		
		try{
			String uid = request.params(":uid");
			
			logger.info("uid: [{}]", uid);
			
			if(!StringUtils.isEmpty(uid)){
				List<String> commands = userCommandMap.get(uid);
				if(commands != null){
					logger.info(String.join(", ", commands));
					
					return getGsonInstance().toJson(commands);
				}
			}
			else{
				logger.error("uid is empty");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "{}";
	}
	
	private static String processGetSelectedSong(Request request, Response response){
		try{
			String uid = request.params(":uid");
			
			logger.info("uid: [{}]", uid);
			
			if(!StringUtils.isEmpty(uid)){
				LinkedList<String> sids = userOrderedSongMap.get(uid);
				if(sids != null){
					LinkedList<Song> orderedSongs = new LinkedList<Song>();
					for(String sid : sids){
						if(!"0".equals(sid)){
							MusicVideo mv = findMusicVideoById(sid);
							orderedSongs.add(convertMusicVideoToSong(mv));
						}
					}
					
					return getGsonInstance().toJson(orderedSongs);
				}
			}
			else{
				logger.error("uid is empty");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "{}";
	}
	
	@SuppressWarnings("unchecked")
	private static String processAddCommand(Request request, Response response){
		response.status(200);
	    response.type("application/json");
	    
	    HashMap<String, String> resultMap = new HashMap<String, String>();
	    
	    try{
	    	HashMap<String, String> parms = getGsonInstance().fromJson(request.body(), HashMap.class);
		    String uid = parms.get("uid");
		    String cmd = parms.get("cmd");
		    
		    logger.info("uid: [{}] | cmd [{}]", uid, cmd);
		    
		    if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(cmd)){
		    	logger.info("critical parms empty: uid[{}], cmd[{}]", uid, cmd);
		    	resultMap.put("result", "failed");
		    }
		    else{
		    	List<String> tmpCmdList = userCommandMap.get(uid);
		    	if(tmpCmdList == null){
		    		tmpCmdList = new ArrayList<String>();
		    	}
		    	if(!tmpCmdList.contains(cmd)){
		    		tmpCmdList.add(cmd);
		    	}
		    	userCommandMap.put(uid, tmpCmdList);
		    	resultMap.put("result", "success");
		    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    	resultMap.put("result", "failed");
	    }
	    
	    logger.info("result: {}", resultMap);
	    return getGsonInstance().toJson(resultMap);
	}
	
	@SuppressWarnings("unchecked")
	private static String processAddSong(Request request, Response response){
		response.status(200);
	    response.type("application/json");
	    
	    HashMap<String, String> resultMap = new HashMap<String, String>();
	    
	    try{
	    	LinkedList<LinkedTreeMap<String, String>> list = getGsonInstance().fromJson(request.body(), LinkedList.class);
	    	
	    	for(LinkedTreeMap<String, String> parms : list){
	    		String uid = parms.get("uid");
			    String sid = parms.get("sid");
			    
			    logger.info("uid: [{}] | sid [{}]", uid, sid);
			    
			    if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(sid)){
			    	logger.info("processAddSong - critical parms empty: uid[{}], sid[{}]", uid, sid);
			    	resultMap.put("result", "failed");
			    }
			    else{
			    	LinkedList<String> tmpSongList = userOrderedSongMap.get(uid);
			    	if(tmpSongList == null){
			    		tmpSongList = new LinkedList<String>();
			    		//tmpSongList.add("0");
			    	}
			    	tmpSongList.add(sid);
			    	
			    	userOrderedSongMap.put(uid, tmpSongList);
			    	resultMap.put("result", "success");
			    }
	    	}
		    
	    }catch(Exception e){
	    	e.printStackTrace();
	    	resultMap.put("result", "failed");
	    }
	    
	    logger.info("result: {}", resultMap);
	    return getGsonInstance().toJson(resultMap);
	}
	
	private static String processRemoveFirstSong(Request request, Response response){
		response.status(200);
	    response.type("application/json");
	    
	    HashMap<String, String> resultMap = new HashMap<String, String>();
	    
	    resultMap.put("result", "failed");
	    
	    try{
	    	String uid = request.params(":uid");
		    
		    logger.debug("Removing song | uid: [{}]", uid);
		    
		    if(StringUtils.isEmpty(uid)){
		    	logger.info("critical parms empty: uid[{}]", uid);
		    }
		    else{
		    	if(userOrderedSongMap.get(uid) == null){
		    		logger.info("no command for uid[{}] found", uid);
		    	}
		    	else{
		    		LinkedList<String> tmpList = userOrderedSongMap.get(uid);
		    		if(tmpList != null && tmpList.size()>0){
		    			tmpList.removeFirst();
		    			resultMap.put("result", "success");
		    		}
		    	}
		    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    logger.info("result: {}", resultMap);
	    return getGsonInstance().toJson(resultMap);
	}
	
	@SuppressWarnings("unchecked")
	private static String processUpdateCommand(Request request, Response response){
		response.status(200);
	    response.type("application/json");
	    
	    HashMap<String, String> resultMap = new HashMap<String, String>();
	    
	    resultMap.put("result", "failed");
	    
	    try{
	    	HashMap<String, String> parms = getGsonInstance().fromJson(request.body(), HashMap.class);
		    String uid = parms.get("uid");
		    String cmd = parms.get("cmd");
		    
		    logger.debug("uid: [{}] | cmd to be removed[{}]", uid, cmd);
		    
		    if(StringUtils.isEmpty(uid) || StringUtils.isEmpty(cmd)){
		    	logger.info("critical parms empty: uid[{}], cmd[{}]", uid, cmd);
		    }
		    else{
		    	if(userCommandMap.get(uid) == null){
		    		logger.info("no command for uid[{}] found", uid);
		    	}
		    	else{
		    		List<String> tmpList = userCommandMap.get(uid);
		    		if(tmpList != null){
		    			tmpList.remove(cmd);
		    			resultMap.put("result", "success");
		    		}
		    	}
		    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    logger.info("result: {}", resultMap);
	    return getGsonInstance().toJson(resultMap);
	}
	
	private static String processNext(Request request, Response response){
		logger.info("call next from IP: {}", request.ip());
		response.status(200);
	    response.type("application/json");
	    
	    try{
			String uid = request.params(":uid");
			
			logger.info("uid: [{}]", uid);
			
			if(!StringUtils.isEmpty(uid)){
				Song currSong = getCurrentSong(uid);
				logger.info("Current song: {}", currSong);
			    if(currSong != null){
			    	return getGsonInstance().toJson(currSong);
			    }
			}
			else{
				logger.error("uid is empty");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "{}";
	}
	
	private static Gson getGsonInstance(){
		return new GsonBuilder()
	    	     .enableComplexMapKeySerialization()
	    	     .serializeNulls()
	    	     .setDateFormat(DateFormat.LONG)
	    	     .setPrettyPrinting()
	    	     .setVersion(1.0)
	    	     .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
	    	     .create();
	}

}
