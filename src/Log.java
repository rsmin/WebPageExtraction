import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Log {
    private static Log ourInstance = new Log();
   private static String path;
   private final static String fileName = "\\log";
   private final static String checkPointProduct = "\\checkPointProduct.txt";
   private final static String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
    public static Log getInstance() {
        return ourInstance;
    }
    private Log() {
        File directory = new File("");
        try {
            path = directory.getAbsolutePath();
        }
        catch(Exception e) {System.out.println("cannot locate current directory");System.out.println(e.getMessage());}
    }

public void writeLog(String content){
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentTime = df.format(new Date()).toString();
    try{
        FileWriter writer = new FileWriter(path+fileName+date+".txt",true);
        writer.write(currentTime+":    "+content);
        writer.write("\r\n");
        writer.flush();
        writer.close();
    }catch (IOException e){
        e.printStackTrace();
    }
}

public void writeCheckPointCategoryProduct(String content){
    try{
        FileWriter writer = new FileWriter(path+checkPointProduct,true);
        writer.write(content);
        writer.write("\r\n");
        writer.flush();
        writer.close();
    }catch (IOException e){
        e.printStackTrace();
    }
}
public Set<String> readCheckPointProduct(){
    Set<String> buffer = new HashSet<String>();
    try {
        FileInputStream fs = new FileInputStream(path + checkPointProduct);
        InputStreamReader inReader = new InputStreamReader(fs,"UTF-8");
        BufferedReader bufReader = new BufferedReader(inReader);
        String line =null;
        int i = 1;
        while((line = bufReader.readLine()) != null){
           buffer.add(line);
            i++;
        }
        bufReader.close();
        inReader.close();
        fs.close();
        return buffer;
    }catch(Exception e){e.printStackTrace(); return buffer;}
}
}
