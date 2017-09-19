import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChemistExtract {
    final String firstLevelProduct = "product-list-container";
    public final String mainPage = "https://www.chemistwarehouse.com.au";
    final String categories = "/categories";
    Set<String> visitedProduct = Log.getInstance().readCheckPointProduct();
    String nextPage = "";
    DBConnection db = new DBConnection();
    Set<String> canotOpenPages = new HashSet<String>();

    String delimiter="。";

    public Map<String, String> categoryExtract() {
        Map<String, String> cateMap = new HashMap();
        DownloadPage dp = new DownloadPage(mainPage + categories);
        Elements categoryElemsTree = dp.doc.select(".CategoryTreeItem");
        for(Element node:categoryElemsTree) {
                String cateName = node.select("span").select(".name").text();
                String cateURL = node.select("a").attr("href");
                if (!cateName.isEmpty() && !cateURL.isEmpty())
                    cateMap.put(cateName, mainPage + cateURL);
        }
        Log.getInstance().writeLog("Category extraction has complete");
        return cateMap;
    }

    public Set<String> ProductsURLInOnePage(String url) {
        Set urlSet = new HashSet();
        DownloadPage dp = new DownloadPage(url);
        if(dp.doc==null) {
            nextPage="";
            return urlSet;
        }
        Elements nextPageElem = dp.doc.select(".next-page");
        if (nextPageElem.isEmpty()||nextPageElem==null) {
            nextPage = "";
        }
        else {
            String nextPageStr = nextPageElem.first().attr("href").trim();
            if (nextPageStr.isEmpty() || nextPageStr == null)
                nextPage = "";
            else if ((mainPage + nextPageStr).trim().equals(url))
                nextPage = "";
            else
                nextPage = (mainPage + nextPageStr).trim();
        }

        Elements elems = dp.doc.select(".product-container");
        if(elems.isEmpty())
            return urlSet;

        for (Element elem : elems) {
            String productURL = elem.attr("href");
            if (!productURL.isEmpty() || productURL != null)
                urlSet.add(mainPage + productURL);
        }
        Log.getInstance().writeLog("ProductsURLInOnePage Extraction done: " + url);
        return urlSet;
    }


    //
    //product_id,product_name,product_url,price,save(0),RRP
    public String productDetail(String url) {
        if(visitedProduct.contains(url)){
            if(canotOpenPages.contains(url))
                canotOpenPages.remove(url);
            return "";
        }

        DownloadPage dp = new DownloadPage(url);
        if(dp.doc==null){
            canotOpenPages.add(url);
            return "";
        }

        Elements elems = dp.doc.select("td");
        if(elems.isEmpty()) {
            return "";
        }
        String productName = elems.select(".product-name").select("[itemprop=name]").text();
        String productURL = url;

        String product_idStr = elems.select(".product-id").text();
        String priceStr = elems.select(".Price").select("[itemprop=price]").text();
        String saveStr = elems.select(".Savings").text();
        String RRPStr = elems.select(".retailPrice").text();
        String productID = productIDFormat(product_idStr);
        String price = priceFormat(priceStr);
        String save = saveFormat(saveStr);
        String RRP = rrpFormat(RRPStr);
        visitedProduct.add(url);
        Log.getInstance().writeCheckPointCategoryProduct(url);
        return productID + delimiter + productName + delimiter + productURL + delimiter + price + delimiter + save + delimiter + RRP;
    }

    //<div class="product-id">Product ID: 63404</div>
    private String productIDFormat(String product_idStr) {
        String result = product_idStr.replace("Product ID:", "").trim();
        if (!result.isEmpty() || result != null)
            return result;
        else return "null";
    }

    //<div class="Price" itemprop="price">$17.97</div>
    private String priceFormat(String priceStr) {
        String result = priceStr.replace("$", "").trim();
        if (!result.isEmpty() || result != null)
            return result;
        else return "0";
    }

    //<div class="Savings" style="display:block">SAVE $17.98</div>
    private String saveFormat(String saveStr) {
        String result = saveStr.replace("SAVE", "").replace("$", "").trim();
        if (!result.isEmpty() || result != null)
            return result;
        else return "0";
    }

    private String rrpFormat(String RRPStr) {
        String result = RRPStr.replace("Don't Pay RRP:", "").replace("$", "").trim();
        if (!result.isEmpty() || result != null)
            return result;
        else return "0";
    }

    //outputLine: //product_id,product_name,product_url,price,save(0),RRP,category
    public void scanner() {
        Map<String, String> cateMap = categoryExtract();
        Log.getInstance().writeLog("cate num: "+cateMap.size());
        int i=0;
        if(!cateMap.isEmpty())
        for (Map.Entry<String, String> entry : cateMap.entrySet()) {
            String startCate = entry.getValue();
            nextPage = startCate;
            while (nextPage != "") {
                Set<String> productCollection = ProductsURLInOnePage(nextPage);
                List<MultiThread> list = new ArrayList<MultiThread>();
                int threadCount =0;
                for (String productURL : productCollection) { //add category
                    MultiThread mt = new MultiThread("Thread"+threadCount, productURL, entry.getKey());
                    mt.start();
                    list.add(mt);
                    threadCount++;
                }
                try {
                    for (MultiThread mt : list) {
                        mt.join();
                    }
                } catch (Exception e) {
                    Log.getInstance().writeLog("join failed. "+e.getMessage());
                }
                Log.getInstance().writeLog("Complete Page: " + nextPage);
                try{
                    Thread.currentThread().sleep(3000);
                }catch(Exception e){e.printStackTrace();}
            }
            Log.getInstance().writeLog("Complete Category "+i+": " + entry.getValue());
            i++;
        }

        Log.getInstance().writeLog("Extraction is complete");
    }

    public void cannotOpenProcess(){
        if(canotOpenPages.isEmpty())
            return;
        String[] cOP = (String[])canotOpenPages.toArray();
        for(String s:cOP){
            String prod = productDetail(s);
            String outptuLine = prod + delimiter + "null";
            //write to database;
            if(db.insert(outptuLine))
                canotOpenPages.remove(s);
        }
    }

    class MultiThread extends Thread {
        String productURL;
        String cateLog;

        public MultiThread(String name, String productURL, String catelog) {
            this.productURL = productURL;
            this.cateLog = catelog;
            this.setName(name);
        }

        public void run() {
            String prod = productDetail(productURL);
            if(prod.isEmpty())
                return;
            String outptuLine = prod + delimiter + cateLog;
            //write to database;
            db.insert(outptuLine);
        }
    }
}

