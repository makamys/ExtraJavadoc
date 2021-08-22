package makamys.extrajavadoc;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

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
	    
        try {
            List<Path> paths = Files.walk(srcDir).filter(Files::isRegularFile).collect(Collectors.toList());
            
    	    for(int i = 0; i < paths.size(); i++) {
    	        Path p = paths.get(i);
    	        System.out.println(String.format("[%d / %d]", i, paths.size()));
    	        Path outPath = outDir.resolve(srcDir.relativize(p));
    	        outPath.getParent().toFile().mkdirs();
    	        if(p.endsWith(".java")) {
    	            Files.write(outPath, getNewSource(p.toFile()).getBytes("utf8"));
    	        } else {
    	            Files.copy(p, outPath);
    	        }
    	    }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private static String getNewSource(File file) throws IOException {
	    JavaClassSource source = Roaster.parse(JavaClassSource.class, file);
        
        //source.getJavaDoc().setText("hello class");
        
        //MethodSource method = source.getMethods().stream().filter(m -> m.getName().equals("main")).findFirst().get();
        //method.getJavaDoc().setText("hello method");
        
	    return source.toString();
	}
	
	private static String getJarName() {
        return new java.io.File(MethodHandles.lookup().lookupClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }
}