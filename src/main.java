import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;

public class main {

    public static void main(String[] args) {
        ChemistExtract CE = new ChemistExtract();

//test category
        /*
        Map<String,String> cateMap = CE.categoryExtract();
        for(Map.Entry<String,String> entry:cateMap.entrySet()){
            System.out.println(entry.getKey()+": "+entry.getValue());
        }
        */

        //test productPage
        /*
        for(String s:CE.ProductsURLInOnePage(CE.mainPage+"/Shop-Online/232/Hair-Removal")){
                System.out.println(s);
        }
        */

        //test productDetails
        /*
        System.out.println(CE.productDetail("https://www.chemistwarehouse.com.au//buy/69591/Veet-Wax-Strips-Sensitive-40"));
        */
        //test nextPage
        /*
        CE.ProductsURLInOnePage(CE.mainPage+"/Shop-Online/232/Hair-Removal");
        System.out.println(CE.nextPage);
        */
        /*
        CE.ProductsURLInOnePage("https://www.chemistwarehouse.com.au/Shop-Online/1724/Revlon-Top-Speed-Nail-Enamel");
*/
          CE.scanner();
          int i = 10;
        while(!CE.canotOpenPages.isEmpty()||i>0) {
            Log.getInstance().writeLog("Start cannotOpenProcess of "+ CE.canotOpenPages.size());
            try{
                Thread.currentThread().sleep(30000);
            }catch(Exception e){e.printStackTrace();}
            CE.cannotOpenProcess();
            i--;
        }
            CE.db.close();
        if(i<=0) {
            Log.getInstance().writeLog("Done with cannot open page");
            Log.getInstance().writeLog("number of cannot open: "+CE.canotOpenPages.size());
        }
        else
            Log.getInstance().writeLog("Done!");
     }
}
