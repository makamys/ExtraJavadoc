package makamys.extrajavadoc;

import java.io.File;
import java.io.IOException;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

class ExtraJavadoc {
	
	public static void main(String[] args){
	    try {
            JavaClassSource type = Roaster.parse(JavaClassSource.class, new File("src/main/java/makamys/extrajavadoc/ExtraJavadoc.java"));
            
            type.getJavaDoc().setText("hello class");
            
            MethodSource method = type.getMethods().stream().filter(m -> m.getName().equals("main")).findFirst().get();
            method.getJavaDoc().setText("hello method");
            System.out.println(type);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}