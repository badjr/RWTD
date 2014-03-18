/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
    public static Scanner userScan;

    public static void main(String[] args) {

//        long startTime = System.currentTimeMillis();
//        TaxSalesSearchResultsURLParser searchResultParser1 = new TaxSalesSearchResultsURLParser();
//        searchResultParser1.grabSearchResultsURLs();
//        long endTime = System.currentTimeMillis();
//        
//        System.out.println("Total execution time: " + (endTime - startTime) );
//        
//        startTime = System.currentTimeMillis();

        Geocoder gs = new Geocoder();

        while (true) {
            System.out.println("Enter a command: ");
            System.out.println("1. Grab tax sale ads");
            System.out.println("2. Fill Lat Long Coords");
            System.out.println("3. Geocode an address");
            System.out.println("4. Tax sale to Fusion");
            System.out.println("q to quit");

            userScan = new Scanner(System.in);

            boolean validCommand = false;

            while (!validCommand) {

                validCommand = true;

                String choice = userScan.next();
                userScan.nextLine(); //bypass the rest of the line so we can get additional input for case 3

                switch (choice) {
                    case "1":
                        grabTaxSaleAds();
                        break;
                    case "2":
                        System.out.println("Now filling lat long coordinates...");
                        gs.fillLatLongCoords();
                        break;
                    case "3":
                        System.out.println("What address do you want to geocode?");
                        String address = userScan.nextLine();
                        System.out.println("Performing Geocode on " + address + "...");
                        try {
                            GeocoderOrig.performGeocode(address);
                        }
                        catch (IOException | XPathExpressionException |
                                ParserConfigurationException | SAXException ex) {
                            Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "4":
                        System.out.println("Converting tax sale .csv to Fusion .csv...");
                        TaxSaleToFusion.taxSaleToFusion();
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

        }

    }

    static void grabTaxSaleAds() {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter number of pages: ");
        int numPages = s.nextInt(); //10

        System.out.print("Enter number of results per page (max 50): ");
        int resultsPerPage = s.nextInt(); //50

        //Read the router file to find the last county searched and period.
        File f = new File("AdsRouterAndKeys\\AdTaxSales\\TSProdRouter.txt");
        String countyToSearch;
        String period;
        String routerCountyAndPeriod[];
        Scanner s2;
        try {
            s2 = new Scanner(f);
            //Router file contains the county and period separated by a comma.
            routerCountyAndPeriod = s2.nextLine().split(",");
            countyToSearch = routerCountyAndPeriod[0];
            period = routerCountyAndPeriod[1];
            s2.close();

            //When we get the county and period from the router file,
            //ask the user if they want to keep these the same.
            System.out.println("Do you want to search in " + countyToSearch + " county? (y/n)");
            String response = s.next();

            while (!(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("n"))) {
                System.out.println("Please enter y or n.");
                response = s.next();
            }

            if (response.equalsIgnoreCase("n")) {
                System.out.print("Enter county to search: ");
                countyToSearch = s.next();
            }

            System.out.println("Is " + period + " the correct period? (y/n)");
            response = s.next();

            while (!(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("n"))) {
                System.out.println("Please enter y or n.");
                response = s.next();
            }

            if (response.equalsIgnoreCase("n")) {
                System.out.print("Enter the period: ");
                countyToSearch = s.next(); //fulton
            }

            //If either the county or period were different, we write the new
            //values to the router file.
            if (!routerCountyAndPeriod[0].equalsIgnoreCase(countyToSearch)
                    || !routerCountyAndPeriod[1].equalsIgnoreCase(period)) {
                FileWriter fw = new FileWriter(f);
                fw.write(countyToSearch + "," + period);
                fw.close();
            }

            //Create numPages threads, each thread will process a page.
            TaxSalesSearchResultsURLParserThread[] searchResultParser2 = new TaxSalesSearchResultsURLParserThread[numPages];
            for (int i = 0; i < 10; i++) {
                searchResultParser2[i] = new TaxSalesSearchResultsURLParserThread(i + 1, resultsPerPage, countyToSearch, period);
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
        catch (IOException ex) {
            Logger.getLogger(RWTD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
