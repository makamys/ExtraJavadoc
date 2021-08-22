package makamys.extrajavadoc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.hjson.JsonValue;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class ExtraJavadoc {
	
	public static void main(String[] args){
	    if(args.length != 2) {
            System.out.println("Usage: java -jar " + getJarName() + " SRCDIR EXTRA_JSON\n\nCreates a copy of SRCDIR, with the javadoc of the java source files within modified according to the rules in EXTRA_JSON.");
            System.exit(1);
        }
	    
	    File srcDirFile = new File(args[0]);
	    Path srcDir = srcDirFile.toPath();
	    File extraJsonFile = new File(args[1]);
	    Path outDir = new File(srcDir.getFileName().toString() + "-extrajavadoc").toPath();
	    
        processCopy(srcDir, outDir, extraJsonFile);
	}
	
	private static void processCopy(Path srcDir, Path outDir, File extraJsonFile) {
	    try {
            String jsonString = JsonValue.readHjson(new FileReader(extraJsonFile)).toString();
            Map extraJson = new Gson().fromJson(jsonString, new TypeToken<Map>(){}.getType());
            Map changes = (Map)extraJson.get("changes");
            
            if(Files.isDirectory(outDir)) {
                Files.walk(outDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
            
            List<Path> paths = Files.walk(srcDir).filter(Files::isRegularFile).collect(Collectors.toList());
            
            for(int i = 0; i < paths.size(); i++) {
                Path p = paths.get(i);
                System.out.println(String.format("[%d / %d] %s", i, paths.size(), srcDir.relativize(p)));
                Path outPath = outDir.resolve(srcDir.relativize(p));
                outPath.getParent().toFile().mkdirs();
                if(p.toString().endsWith(".java")) {
                    Files.write(outPath, getNewSource(p.toFile(), changes).getBytes("utf8"));
                } else {
                    Files.copy(p, outPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private static String getNewSource(File file, Map allChanges) throws IOException {
	    JavaType type = Roaster.parse(file);
	    if(type instanceof JavaClassSource) {
	        JavaClassSource source = (JavaClassSource)type;
	        
	        Map changes = (Map)allChanges.get(source.getCanonicalName());
	        
	        if(changes != null) {
	            String classChanges = (String)changes.get("class");
	            if(classChanges != null) {
	                Parser parser = Parser.builder().build();
	                Node document = parser.parse(classChanges);
	                HtmlRenderer renderer = HtmlRenderer.builder().build();
	                source.getJavaDoc().setText(renderer.render(document));
	            }
	        }
	    }
	    return type.toString();
	}
	
	private static String getJarName() {
        return new java.io.File(MethodHandles.lookup().lookupClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }
}