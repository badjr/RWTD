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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Brett
 */
/*
 * Sequential version.
 */
public class TaxSalesSearchResultsURLParser {

    public void grabSearchResultsURLs() {
        String str = "";
        URL searchResultsURL; //This is the URL of the search results page when selecting the category and the county and pressing search.

        String searchResultsURLSource = ""; //This string stores the source of the search results page.

        String countyToSearch = "fulton"; //Maybe later we can have the user set this
//        int pageNum = 1;
//        int numURLs;
        for (int pageNum = 1; pageNum <= 10; pageNum++) {
//            numURLs = 0;
            searchResultsURLSource = ""; //Reset this
            try {
//            searchResultsURL = new URL("http://georgiapublicnotice.com/pages/results_content?category=gpn20&phrase_match=&min_date=&max_date=&page_label=home&widget=search_content&string=search_category_gpn20+search_county_cobb&county=Cobb+County");
                searchResultsURL = new URL("http://georgiapublicnotice.com/pages/"
                        + "results_content/push?per_page=50&x_page=" + pageNum
                        + "&rel=next&class=&search_content[category]=gpn20"
                        + "&search_content[phrase_match]="
                        + "&search_content[min_date]="
                        + "&search_content[max_date]="
                        + "&search_content[page_label]=results_content"
                        + "&search_content[string]=search_category_gpn20"
                        + "+search_county_" + countyToSearch
                        + "&search_content[county]=" + countyToSearch + "+County");

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(searchResultsURL.openStream()));

                while ((str = in.readLine()) != null) {
//                str = in.readLine().toString();
//                    searchResultsURLSource += str + "\r\n"; // \r\n is for windows
                    searchResultsURLSource += str + System.lineSeparator();
//                System.out.println(str);
                }
                in.close();
            }
            catch (MalformedURLException ex) {
                Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException e) {
            }

            //Parsing searchResultsURLSource for the search result strings. 
            //They are formatted like:
            // /view/full_story/24406702/article-M-9036-NOTICE-OF-FORECLOSURE-OF-RIGHT-TO-REDEEM--REF--O-C-G-A-?
            Scanner urlSourceScanner = new Scanner(searchResultsURLSource);
            String line;
            String storyURLLink;
            int numURLs = 0;
            while (urlSourceScanner.hasNextLine()) {
                line = urlSourceScanner.nextLine();
                if (line.contains("story_item_full_story_link")) { //If the line contains "story_item_full_story_link"
                    //Sample line:  
                    //<a class="story_item_full_story_link" href="/view/full_story/24520322/article-CLARK-CASKEY--LLC-NOTICE-OF-SERVICE-BY-PUBLICATION-RGB-VENTURE-CAPITAL--LLC-V--OSTER-SHEWARD-PROPERTIES--LLC--C--LEWIS-HUDSON--WELLS-FARGO-BANK--N-A--AS-SUCCESSOR-TO-WACHOVIA-BANK--THE-PRODUCTIVITY-COMPANY--FULTON-COUNTY--AND-CITY-OF-UNION?">full story</a></span>
                    //We parse from /view/full_story/ to ">
                    storyURLLink = line.substring(line.indexOf("/view/full_story"), line.indexOf("\">")); //Get the substring, store it in storyURLLink
                    if (storyURLLink != null) {
                        numURLs++;
//                        System.out.println(storyURLLink); //Print out that URL
                    }
                }
            }

            System.out.println(numURLs + " URLs found on page " + pageNum);
        }
        //Jsoup is a library that parses HTML sources and extracts just the text
        //on that web page.
//        Document doc = Jsoup.parse(searchResultsURLSource);
//        String textOnWebpage = doc.body().text(); //The text of the web page is stored in textOnWebpage

        

    }
}
