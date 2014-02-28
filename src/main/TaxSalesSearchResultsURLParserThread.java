/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author Brett
 */
/*
 * Threaded version. Runs faster than the sequential version.
 */
//public class TaxSalesSearchResultsURLParserThread implements Runnable {
//TODO: Write all source URLs to files
//TODO: Write ads to files
public class TaxSalesSearchResultsURLParserThread extends Thread {

    private int myID; //Corresponds to the search results page a thread will process.
    private String[] storyURLsArray; //This array holds the story URLs found on the search results page.
    private String csvString = "";
//    private static String fileName; //File name of .csv to write to. Static so each thread will write to the same file.
    private int numAdsParsed = 0;
    private int searchResultsAdsPerPage;
    private String countyToSearch;

    public TaxSalesSearchResultsURLParserThread(int myID, int searhResultsAdsPerPage, String countyToSearch) {
        this.myID = myID;
        this.searchResultsAdsPerPage = searhResultsAdsPerPage;
        this.countyToSearch = countyToSearch;
    }

    public void grabSearchResultsURLs() {
        String str;

        String searchResultsURLSource = ""; //This string stores the source of the search results page.

//        countyToSearch = "fulton"; //Maybe later we can have the user set this
        storyURLsArray = new String[searchResultsAdsPerPage]; //50 stories max per page, it appears.

        try {
            //This is the URL of the search results page when selecting the category and the county and pressing search.
            URL searchResultsURL = new URL("http://georgiapublicnotice.com/pages/"
                    + "results_content/push?per_page=" + searchResultsAdsPerPage
                    + "&x_page=" + myID
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
        int numURLs = 0;

        while (urlSourceScanner.hasNextLine()) {
            String line = urlSourceScanner.nextLine();
            if (line.contains("story_item_full_story_link")) { //If the line contains "story_item_full_story_link"
                //Sample line:  
                //<a class="story_item_full_story_link" href="/view/full_story/24520322/article-CLARK-CASKEY--LLC-NOTICE-OF-SERVICE-BY-PUBLICATION-RGB-VENTURE-CAPITAL--LLC-V--OSTER-SHEWARD-PROPERTIES--LLC--C--LEWIS-HUDSON--WELLS-FARGO-BANK--N-A--AS-SUCCESSOR-TO-WACHOVIA-BANK--THE-PRODUCTIVITY-COMPANY--FULTON-COUNTY--AND-CITY-OF-UNION?">full story</a></span>
                //We parse from /view/full_story/ to ">
                String storyURLLink = line.substring(line.indexOf("/view/full_story"), line.indexOf("\">")); //Get the substring, store it in storyURLLink
                if (storyURLLink != null) {
                    storyURLsArray[numURLs] = storyURLLink;
                    numURLs++;
//                        System.out.println(storyURLLink); //Print out that URL
                }
            }
        }

        System.out.println(numURLs + " URLs found on page " + myID);

    }

    /*
     * Grabs the ads from GOVERNMENT TAX SERVICES, LLC. sheriff sale ads only,
     * then saves to a .csv file.
     */
    public void grabAdsFromStoryURLs() {

        /*
         * GOVERNMENT TAX SERVICES, LLC. has parsable information in only their
         * sheriff sale ads.
         * I encountered a BELLWOOD ENTERPRISES, LLC ad which does not have
         * easily parsable information.
         */

        /*
         * Some GOVERNMENT TAX SERVICES, LLC. ads sometimes have a
         * tax transferee field. Added a special case for the ads that don't
         * contain this field to leave it blank.
         */

        //we have these fields:
        //Sheriff Sale #, TAX PARCEL ID, CURRENT RECORD HOLDER, 
        //DEFENDANT IN FIFA, AMOUNT DUE, TAX YEARS DUE, DEED BOOK, 
        //LEGAL DESCRIPTION

        URLSourceGrabber urlSourceGrabber = new URLSourceGrabber();

        for (int i = 0; i < storyURLsArray.length; i++) {
            String URLSource = urlSourceGrabber.getSourceFromURL("http://georgiapublicnotice.com/" + storyURLsArray[i]);

            //When we get the URL source of the stories from the search results
            //page, we call this method to write them to their separate files.
            writeStoryURLSourceToFile(URLSource, i);

            try { //Get only government tax sheriff sale ads.

                //The string that will store the current ad.
                String currTaxServicesSaleAd = URLSource.substring(URLSource.indexOf("<br />SHERIFF SALE #:"), URLSource.indexOf("<br>#")).substring(6);
                //add on the substring(6) to get that early <br /> off, then we can use <br> as a delimiter

                //Using <br> as the delimiter, the ad is now split into its fields
                //and stored in adSplitByColon[].
                String adSplitByColon[] = currTaxServicesSaleAd.split("<br>");

//                System.out.println("------------------------------------");
                for (int j = 0; j < adSplitByColon.length; j++) {

                    if (adSplitByColon.length == 8 && j == 4) {
                        //Some ads don't have a tax transferree.
                        //These ads have 8 fields instead of 9, so when we get
                        //to column 4, we leave it blank and continue.
                        csvString += ",,";
                        continue;
                    }

                    //Open with quotes so any commas from within a cell don't get separated in the .csv file.
                    csvString += "\"";

                    String currField = adSplitByColon[j];

                    //Add 1 to trim that beginning space after the :
                    csvString += currField.substring(currField.indexOf(":") + 1).trim();

                    //End with quotes so any commas from within a cell don't get separated in the .csv file.
                    csvString += "\",";
                }

//                System.out.println("------------------------------------");

                csvString += System.lineSeparator();
                numAdsParsed++;
            }
            catch (Exception e) {
                //If we get this exception, then it's not a gov tax sherrif sale
                //ad and we do nothing.
            }

            //Write the URL Source to file

        }

//        System.out.println("----------------------");
//        System.out.println(csvString);
//        System.out.println("----------------------");

    }

    /*
     * This method writes the actual story URLs to separate .txt files for
     * investigating parsing errors or discrepancies. 
     * @param URLSource The string holding the URL source of a story
     * @param i The index in the array storing the story URLs. Corresponds to
     * the result number on a page.
     */
    private void writeStoryURLSourceToFile(String URLSource, int i) {
        //FIXME: This method is potentially slow.
        //Build times:
        //6 min 20 sec
        //54 sec
        //13 minutes 34 seconds
        //3 minutes 6 seconds
        //28 minutes 12 seconds
        
        //DateTime object from joda-time library.
        //We'll use this to get the month, day of month, and year to use in
        //the directory we will write.
        DateTime dt = new DateTime();
        
        try {
            File f = new File("AdsTaxSaleGA" + "\\AdsTaxSaleSourceGA\\"
                    + countyToSearch + "GA"
                    + "\\" + dt.getMonthOfYear() + "-" + dt.getDayOfMonth()
                    + "-" + (dt.getYear() % 100)
                    + "\\pg" + myID + "result" + i + ".txt");
            
            //Make the directory if it doesn't exist yet.
            f.mkdirs();
            
            //If the file already exists, delete then recreate a blank file.
            if (f.exists()) {
                f.delete();
                try {
                    f.createNewFile();
                }
                catch (IOException ex) {
                    Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            //Write the URL source to the file.
            FileWriter fw = new FileWriter(f);
            fw.write(URLSource);
            fw.close(); //Close the FileWriter.
        }
        catch (IOException ex) {
            Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * This method writes all story URLs from the search results page to a .txt
     * file.
     */
    public void writeStoryURLsToFile() {
        String fileName = "URLsAdsTaxSale" + countyToSearch + ".txt";
        String path = "AdsTaxSaleGA\\URLsAdsTaxSaleGA\\";
        File f = new File(path + fileName);
        
        if (myID == 1) { //Use only thread 1 for checking if the file exists.
            if (f.exists()) { //Delete and create a new blank file if it existed.
                f.delete();
                try {
                    f.createNewFile();
                }
                catch (IOException ex) {
                    Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //Writing the story URLs from storyURLsArray to file.
        FileWriter fw = null;
        try {
            fw = new FileWriter(path + fileName, true);
            for (int i = 0; i < storyURLsArray.length; i++) {
                fw.write("http://georgiapublicnotice.com/pages" + storyURLsArray[i] + System.lineSeparator());
            }
        }
        catch (IOException ex) {
            Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                fw.close();
            }
            catch (IOException ex) {
                Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void writeTaxSaleAdsToFile() {
        //TODO: Decide on the file name to write to, and if we keep multiple
        //files if file already exists.

        DateTime dt = new DateTime(); //For putting the month, year, and date in file name.
        String fileName = "AdsTaxSaleGA\\AdsTaxSaleGA"
                + "\\AdsTaxSaleGA"
                + "\\" + countyToSearch + "GA"
                + "\\govTaxSaleAds" 
                + dt.getMonthOfYear() + "-" + dt.getDayOfMonth()
                + "-" + (dt.getYear() % 100) + ".csv";
        
        File f;

        if (myID == 1) { //Use thread 1 to create the file.
            f = new File(fileName);
            
            //If the file exists already, delete it and create a new blank file.
            if (f.exists()) {
                f.delete();
                try {
                    f.createNewFile();
                }
                catch (IOException ex) {
                    Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        try (FileWriter fw = new FileWriter(fileName, true)) {
//            synchronized (this) {
            if (myID == 1) {
                //Writing the field names on the first row of the .csv
                //Use only thread with ID 1 so every thread doesn't write
                //this row.
                String fields = "SHERIFF SALE #,"
                        + "TAX PARCEL ID, "
                        + "CURRENT RECORD HOLDER,"
                        + "DEFENDANT IN FIFA,"
                        + "TAX TRANSFEREE,"
                        + "AMOUNT DUE,"
                        + "TAX YEARS DUE,"
                        + "DEED BOOK,"
                        + "LEGAL DESCRIPTION"
                        + System.lineSeparator();
                fw.write(fields);
            }

            System.out.println("myID = " + myID + ". Writing " + numAdsParsed + " ads to " + fileName);
            fw.write(csvString);

//                System.out.println("govTaxSaleAdsPage.csv created. " + numAdsParsed + " ads stored.");
//            }
//            System.out.println("govTaxSaleAdsPage" + myID + ".csv created. " + numAdsParsed + " ads stored.");
        }
        catch (IOException ex) {
            //TODO: Pause if don't have permission to write to file.
            Logger.getLogger(TaxSalesSearchResultsURLParserThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        grabSearchResultsURLs();
        grabAdsFromStoryURLs();

    }
}
