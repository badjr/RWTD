/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Brett
 */
/*
 * The purpose of this class is to grab the source code of any web page.
 */
public class URLSourceGrabberThread extends Thread {

    private String urlString;

    public URLSourceGrabberThread(String urlString) {
        this.urlString = urlString;
    }

    public String getSourceFromURL() {
        String sourceFromURL = "";
        URL url;
        String str;
        try {
//            searchResultsURL = new URL("http://georgiapublicnotice.com/pages/results_content?category=gpn20&phrase_match=&min_date=&max_date=&page_label=home&widget=search_content&string=search_category_gpn20+search_county_cobb&county=Cobb+County");
            url = new URL(urlString);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            while ((str = in.readLine()) != null) {
//                str = in.readLine().toString();
                sourceFromURL += str + "\r\n";
//                System.out.println(str);
            }
            in.close();
        }
        catch (MalformedURLException ex) {
            Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException e) {
        }

        Document doc = Jsoup.parse(sourceFromURL);
        String textOnWebpage = doc.body().text();
//        System.out.println("------------------------------------");
//        System.out.println(textOnWebpage);
//        System.out.println("------------------------------------");

        return sourceFromURL;
    }

    @Override
    public void run() {
        getSourceFromURL();
    }
}
