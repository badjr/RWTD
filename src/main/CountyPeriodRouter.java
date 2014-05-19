/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
/**
 *
 * Keeps track of the current period: Apr, May, etc., and year.
 */
public class CountyPeriodRouter {
    
    private File f;
    private String county;
    private String period;
    
    /**
     * On initialization, we read the router file stored in
     * AdsRouterAndKeys/AdTaxSales/TSProdRouter.txt and initialize the
     * county and period fields.
     */
    public CountyPeriodRouter() {
        try {
            //Read the router file to find the last county searched and period.
            f = new File("AdsRouterAndKeys/AdTaxSales/TSProdRouter.txt");
            String routerCountyAndPeriod[]; //Stores the county and period
            Scanner s2 = new Scanner(f);
            
            //Router file contains the county and period separated by a comma.
            routerCountyAndPeriod = s2.nextLine().split(",");
            county = routerCountyAndPeriod[0];
            period = routerCountyAndPeriod[1];
            s2.close();
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(CountyPeriodRouter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getCounty() {
        return county;
    }

    public String getPeriod() {
        return period;
    }

    public void updatePeriod(String newCountyToSearch, String newPeriod) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(newCountyToSearch + "," + newPeriod);
            fw.close();
        }
        catch (IOException ex) {
            Logger.getLogger(CountyPeriodRouter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
