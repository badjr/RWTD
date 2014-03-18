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

/**
 *
 * @author Brett
 */
public class TaxSaleToFusion {

    public static void taxSaleToFusion() {
        File f = new File("AdsTaxSaleProdFultonSep13GATestFullNoAd.csv");
        try {
            Scanner fileScanner = new Scanner(f);
            /*
             * The following are to always be included:
             * Place photo here, FileNoCounty, ParcelID, AddressSite, ZipSite, Amount, Interest, NotesRob, NotesWard, OwnerAd
             * Add the following columns if the user answers yes to Assessor Data:
             * OwnerAddressAsses, LandUseAsses, YearBuilt, SqFtAsses, AcresAsses, LandAppraisedAsses, BuildingAppraisedAsses, TotalAppraisedAsses
             */
            String outString = "Photo,FileNoCounty,ParcelID,AddressSite,ZipSite,Amount,Interest,NotesRob,NotesWard,OwnerAd";

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
            
            //Next, we go through the rest of the old file to get the data.
            while (fileScanner.hasNextLine()) { //For each row
                String currLine = fileScanner.nextLine();
                String colDataOldFile[] = currLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); //This regex splits by commas, skipping over quotes
                
                //For putting the data in column by column
                for (int i = 0; i < colLabelsFusion.length; i++) {
                    if (i == 0) { //Image goes at index 0.
                        //TODO: Put URLs of actual images here.
                        outString += "http://icons.iconarchive.com/icons/emoopo/darktheme-folder/256/Folder-Nature-Stones-icon.png,";
                    }
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
            try {
                //TODO: Find out what to name the tax sale Fusion file.
                FileWriter fw = new FileWriter("TaxSaleFusion.csv");
                fw.write(outString);
                fw.close();
            }
            catch (IOException ex) {
                Logger.getLogger(TaxSaleToFusion.class.getName()).log(Level.SEVERE, null, ex);
            }

            fileScanner.close(); //Closing the file scanner.
            System.out.println("Wrote Fusion .csv to TaxSaleFusion.csv.");
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(TaxSaleToFusion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
