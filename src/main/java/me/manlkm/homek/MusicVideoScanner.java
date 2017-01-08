/**
 * 
 */
package me.manlkm.homek;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.utils.StringUtils;

/**
 * @author manlkm
 *
 */
public class MusicVideoScanner {
	private static Logger logger = LoggerFactory.getLogger(MusicVideoScanner.class);
	private static Integer generatedId = 1;
	static List<MusicVideo> mvList = null;
	
	public static List<MusicVideo> buildMusicVideoList(String pathToScan) throws Exception {
		if(!StringUtils.isEmpty(pathToScan) && new File(pathToScan).exists()){
			logger.info("starting to scan {}", pathToScan);
			
			 mvList = new ArrayList<MusicVideo>();
			 Path p = Paths.get(pathToScan);
			    FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			      @Override
			      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			          throws IOException {
			        
			    	  MusicVideo mv = new MusicVideo();
			    	  mv.setId(String.valueOf(generatedId++));
			    	  mv.setFileType("video/mp4");
			    	  mv.setCat(MusicVideoCat.findByValue(file.getFileName().toString().split("-")[0]));
			    	  mv.setSinger(file.getFileName().toString().split("-")[1]);
			    	  mv.setSongName(file.getFileName().toString().split("-")[2]);
			    	  mv.setFullFileName(file.getFileName().toString());
			    	  mvList.add(mv);
			    	  
			        return FileVisitResult.CONTINUE;
			      }
			    };

			    try {
			      Files.walkFileTree(p, fv);
			    } catch (IOException e) {
			      e.printStackTrace();
			    }
		}
		return mvList;
	}
}
