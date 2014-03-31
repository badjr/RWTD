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
public class TaxSaleToFusion {
    
    /**
     * Converts Tax Sale .csv to a Fusion compatable .csv.
     */
    public static void taxSaleToFusion() {
        //TODO: How to determine file name?
        String taxSaleFileName = "C:\\Dropbox\\AdsTaxSaleProdFultonApr14GA2.csv";
        
        File f = new File(taxSaleFileName);
        try {
            Scanner fileScanner = new Scanner(f);
            /*
             * The following are to always be included:
             * Place photo here, FileNoCounty, ParcelID, AddressSite, ZipSite, Amount, Interest, NotesRob, NotesWard, OwnerAd
             * Add the following columns if the user answers yes to Assessor Data:
             * OwnerAddressAsses, LandUseAsses, YearBuilt, SqFtAsses, AcresAsses, LandAppraisedAsses, BuildingAppraisedAsses, TotalAppraisedAsses
             */
            String outString = "PhotoURL,Latitude,Longitude,FileNoCounty,ParcelID,AddressSite,ZipSite,Amount,Interest,NotesRob,NotesWard,OwnerAd";

            System.out.println("Do you want to include assessor data in the Fusion file? (y/n)");
            String userChoice = RWTD.userScan.next();

            while (!(userChoice.equalsIgnoreCase("y") || userChoice.equalsIgnoreCase("n"))) {
                System.out.println("Please enter either y or n.");
                userChoice = RWTD.userScan.next();
            }

            if (userChoice.equalsIgnoreCase("y")) {
                outString += ",OwnerAddressAsses,LandUseAssesRaw,YearBuiltAsses,SqFtAsses,AcresAsses,LandAppraisedAsses,BuildingAppraisedAsses,TotalAppraisedValueAsses";
            }

            String colLabelsFusion[] = outString.split(","); //Holds the column labels for the Fusion .csv

            //Add the new line after adding the column labels to the colLabelsFusion array.
            outString += System.lineSeparator();

            //First, we get the first line of the old file and get the column labels.
            String oldFileColLabels[] = fileScanner.nextLine().split(","); //Holds the column labels of the old file.
            int oldFileColIndex[] = new int[colLabelsFusion.length]; //oldFilecolIndex will be used to hold the indexes of the column labels from the old file.

            //This nested loop will fill the oldFileColIndex array with the indices 
            //of the column labels from the old file.
            for (int i = 0; i < oldFileColIndex.length; i++) {
                for (int j = 0; j < oldFileColLabels.length; j++) {
                    if (colLabelsFusion[i].equals(oldFileColLabels[j])) {
                        oldFileColIndex[i] = j;
                        break;
                    }
                }
            }
            
            //For debugging mismatched columns
//            System.out.println(Arrays.asList(oldFileColLabels));
//            for (int i = 0; i < oldFileColIndex.length; i++) {
//                System.out.print(oldFileColIndex[i] + " ");
//            }
//            System.out.println();
//            System.out.println(Arrays.asList(colLabelsFusion));
            
            //For geocoding
            int addressColumnIndex = -1;
            int zipColumnIndex = -1;
            
            for (int i = 0; i < oldFileColLabels.length; i++) {
                switch (oldFileColLabels[i]) {
                    case "AddressSite":
                        addressColumnIndex = i;
                        break;
                    case "ZipSite":
                        zipColumnIndex = i;
                        break;
                }
            }            
                      
            int currRow = 1; //This is concatenated with the file name.
            //Next, we go through the rest of the old file to get the data.
            while (fileScanner.hasNextLine()) { //For each row
                String currLine = fileScanner.nextLine();
                String colDataOldFile[] = currLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //This regex splits by commas, skipping over quotes

                //Do geocode at this step to get the lat long coordinates.
//                Geocoder geocoder = new Geocoder();
//                try {
////                    System.out.println("address = " + colDataOldFile[addressColumnIndex] + " " + colDataOldFile[zipColumnIndex]);
//                    geocoder.performGeocode(colDataOldFile[addressColumnIndex] + " " + colDataOldFile[zipColumnIndex]);
//                    Thread.sleep(350); //Add a delay because Google limits how fast we can make geocoding requests.
//                }
//                catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException | InterruptedException ex) {
//                    Logger.getLogger(TaxSaleToFusion.class.getName()).log(Level.SEVERE, null, ex);
//                }
                
                //For generating the file name
                String fullAddress = colDataOldFile[addressColumnIndex];
                String addressSplit[] = fullAddress.split("\\s+");
                String firstThreeOfAddress = addressSplit[1].substring(0, 3);
                                
                //For putting the data on the row column by column
                for (int i = 0; i < colLabelsFusion.length; i++) {
                    if (i == 0) { //Image goes at index 0.
                        //TODO: These are hardcoded for now. Determine how the
                        //file name can be generated automatically.
                        outString += "http://192.155.94.95/TS" 
//                                + countyPeriodRouter.getCounty() 
//                                + countyPeriodRouter.getPeriod() 
                                + "Fulton"
                                + "Apr14"
                                + "/TS"
//                                + countyPeriodRouter.getCounty()
//                                + countyPeriodRouter.getPeriod()
                                + "Fulton"
                                + "Apr14"
                                + firstThreeOfAddress.toUpperCase() 
                                + currRow++ + ".jpg,";
                    }
//                    else if (colLabelsFusion[i].equals("Latitude")) {
////                        outString += geocoder.getLat() + ",";
//                    }
//                    else if (colLabelsFusion[i].equals("Longitude")) {
////                        outString += geocoder.getLng() + ",";
//                    }
                    else {
                        //We use oldFileColIndex to get the proper index from colDataOldFile
                        outString += colDataOldFile[oldFileColIndex[i]] + ((i <= colLabelsFusion.length - 2) ? "," : "");
                        //The conditional checks if it's before or equal to the
                        //2nd to last column. If it is, add a comma.
                        //If not, no need to add a comma after the last column.
                    }
                }
                //After writing all of the columns, add a new line to start the new row.
                outString += System.lineSeparator();
            }

            //outString now contains all of the contents ready to be written to
            //to the Fusion .csv. We write here.
            //Cut off the .csv and concatenate Fusion.csv.
            String taxSaleFusionFileName = taxSaleFileName.substring(0, taxSaleFileName.length() - 4) + "Fusion.csv";
            try {
                //TODO: Find out what to name the tax sale Fusion file.
                FileWriter fw = new FileWriter(taxSaleFusionFileName);
                fw.write(outString);
                fw.close();
            }
            catch (IOException ex) {
                Logger.getLogger(TaxSaleToFusion.class.getName()).log(Level.SEVERE, null, ex);
            }

            fileScanner.close(); //Closing the file scanner.
            System.out.println("Wrote Fusion .csv to " + taxSaleFusionFileName);
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(TaxSaleToFusion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
