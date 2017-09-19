import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class DownloadPage{
    Document doc;
    public DownloadPage(String url){
        doc = readFromHtml(url);
        if(doc==null) {
            Log.getInstance().writeLog("can't not open: " + url);
            try{
                Thread.currentThread().sleep(3000);
            }catch(Exception e){e.printStackTrace();}
        }
        //else
            //Log.getInstance().writeLog("webpage loaded: "+url);
    }
    Document readFromHtml(String url) {
        String inputURL = url;
        try {
            return Jsoup.connect(url).timeout(10000).get();
        }
        catch (Exception e){
            Log.getInstance().writeLog("cannot load html: " +inputURL +"  error message:"+e.getMessage());
            return null;
            }
    }
}