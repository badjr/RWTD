/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author Brett
 */
public class RWTD {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        long startTime = System.currentTimeMillis();
//        TaxSalesSearchResultsURLParser searchResultParser1 = new TaxSalesSearchResultsURLParser();
//        searchResultParser1.grabSearchResultsURLs();
//        long endTime = System.currentTimeMillis();
//        
//        System.out.println("Total execution time: " + (endTime - startTime) );
//        
//        startTime = System.currentTimeMillis();
        GeocodingSample gs = new GeocodingSample();
        while (true) {
            System.out.println("Enter a command: ");
            System.out.println("1. Grab tax sale ads");
            System.out.println("q to quit");

            Scanner s = new Scanner(System.in);

            boolean validCommand = false;

            while (!validCommand) {

                validCommand = true;

                String choice = s.next();

                switch (choice) {
                    case "1":
                        grabTaxSaleAds();
                        break;
                    case "2":
//                        GeocodingSample gs = new GeocodingSample();
//                        try {
                        //                        GeocodeTest.geocode();
//                            GeocodingSample.performGeocode("33 Gilmer St Atlanta, GA");
//                            gs.performGeocode("85 Lightwood Dr");
//                            gs.performGeocode("4010 annecy drive");
//                            System.out.println(gs.getLat());
//                            System.out.println(gs.getLng());
//                            gs.performGeocode("938 cascade avenue sw");
//                            System.out.println(gs.getLat());
//                            System.out.println(gs.getLng());
                        gs.fillLatLongCoords();
//                        }
//                        catch (IOException | XPathExpressionException |
//                                ParserConfigurationException | SAXException ex) {
//                            Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        break;
                    case "3":
                        try {
                            gs.performGeocode("727 FORMWALT ST 30315");
                        }
                        catch (IOException | XPathExpressionException |
                                ParserConfigurationException | SAXException ex) {
                            Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "q":
                    case "Q":
                        System.exit(0);
                        break;
                    default:
                        validCommand = false;
                        System.out.println("Invalid command.");
                }

            }
//            TaxSalesSearchResultsURLParserThread[] searchResultParser2 = new TaxSalesSearchResultsURLParserThread[10];
//            for (int i = 0; i < 10; i++) {
//                searchResultParser2[i] = new TaxSalesSearchResultsURLParserThread(i + 1, 50, "Fulton");
//                searchResultParser2[i].start();
//            }
//
//            for (TaxSalesSearchResultsURLParserThread thread : searchResultParser2) {
//                //This causes threads to execute in order by ID.
//                thread.join();
//                thread.writeStoryURLsToFile();
//                thread.writeTaxSaleAdsToFile();
//            }
        }

//        ExecutorService executor = Executors.newFixedThreadPool(10);
//        for (int i = 0; i < 10; i++) {
//            executor.submit(new TaxSalesSearchResultsURLParserThread(i+1));
//        }
//        executor.shutdown();

//        for (int i = 0; i < 9; i++) {
//            searchResultParser2[i].join();
//        }
//        
//        endTime = System.currentTimeMillis();
//        
//        System.out.println("Total execution time: " + (endTime - startTime) );

    }

    static void grabTaxSaleAds() {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter number of pages: ");
        int numPages = s.nextInt(); //10

        System.out.print("Enter number of results per page (max 50): ");
        int resultsPerPage = s.nextInt(); //50

        System.out.print("Enter county to search: ");
        String countyToSearch = s.next(); //fulton

        TaxSalesSearchResultsURLParserThread[] searchResultParser2 = new TaxSalesSearchResultsURLParserThread[numPages];
        for (int i = 0; i < 10; i++) {
            searchResultParser2[i] = new TaxSalesSearchResultsURLParserThread(i + 1, resultsPerPage, countyToSearch);
            searchResultParser2[i].start();

        }

        for (TaxSalesSearchResultsURLParserThread thread : searchResultParser2) {
            try {
                //This causes threads to execute in order by ID.
                //Also causes the parent thread to wait for all children threads
                //to finish their execution.
                thread.join();
            }
            catch (InterruptedException ex) {
                Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Thread was interrupted!");
            }
            thread.writeStoryURLsToFile();
            thread.writeTaxSaleAdsToFile();
        }
    }
}
