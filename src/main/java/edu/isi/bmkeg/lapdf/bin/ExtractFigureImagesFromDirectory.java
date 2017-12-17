package edu.isi.bmkeg.lapdf.bin;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDPage;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDirection;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.RTree.RTChunkBlock;
import edu.isi.bmkeg.lapdf.model.spatial.SpatialEntity;

/**
 * This script runs through the digital library and extracts all fragments for a
 * given corpus
 * 
 * @author Gully
 * 
 */
public class ExtractFigureImagesFromDirectory {

	public static class Options {

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outdir;

		@Option(name = "-pdfDir", usage = "PDF", required = true, metaVar = "PDF")
		public File pdfDir;

	}

	private static Logger logger = Logger.getLogger(ExtractFigureImagesFromDirectory.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();
		Pattern patt = Pattern.compile("^(Figure|Fig\\.{0,1})\\s+(\\d+)");

		CmdLineParser parser = new CmdLineParser(options);

		try {

			parser.parseArgument(args);

			LapdfEngine eng = new LapdfEngine();
			
			Pattern pattern = Pattern.compile("\\.(.*)$");
			
			String[] fileTypes = {"pdf"};
			
			@SuppressWarnings("unchecked")
			Iterator<File> it = FileUtils.iterateFiles(options.pdfDir, fileTypes, true);
			while( it.hasNext() ) {
				File pdf = it.next();
				
				String newPath = pdf.getPath().replaceAll(
						options.pdfDir.getPath(),
						options.outdir.getPath());
				
				newPath = newPath.substring(0, newPath.indexOf(".pdf"));
				String stem = pdf.getName().substring(0,pdf.getName().length()-4);
				File newDir = new File(newPath);
				
				if( newDir.exists() ) {
					continue;
				}
				newDir.mkdirs();
				
				try {
					Map<String, BufferedImage> imageList = eng.extractFiguresFromArticle(pdf);
					
					List<String> keys = new ArrayList<String>(imageList.keySet());
					Collections.sort(keys);
					for(String name : keys) {

						BufferedImage img = imageList.get(name);
						File outputfile = new File(newPath + "/" + stem + "_fig_" + name + ".png");
						ImageIO.write(img, "png", outputfile);

						logger.info("Extracting image to " + outputfile.getPath());
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			}

		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.print("Arguments: ");
			parser.printSingleLineUsage(System.err);
			System.err.println("\n\n Options: \n");
			parser.printUsage(System.err);
			System.exit(-1);

		} catch (Exception e2) {

			e2.printStackTrace();

		}

	}

}
