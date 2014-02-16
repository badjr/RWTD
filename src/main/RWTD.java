/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Brett
 */
public class RWTD {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        
//        long startTime = System.currentTimeMillis();
//        TaxSalesSearchResultsURLParser searchResultParser1 = new TaxSalesSearchResultsURLParser();
//        searchResultParser1.grabSearchResultsURLs();
//        long endTime = System.currentTimeMillis();
//        
//        System.out.println("Total execution time: " + (endTime - startTime) );
//        
//        startTime = System.currentTimeMillis();
        
        TaxSalesSearchResultsURLParserThread[] searchResultParser2 = new TaxSalesSearchResultsURLParserThread[10];
        for (int i = 0; i < 10; i++) {
            searchResultParser2[i] = new TaxSalesSearchResultsURLParserThread(i+1);
            searchResultParser2[i].start();
        }
        
        for (TaxSalesSearchResultsURLParserThread thread : searchResultParser2) {
            //This causes threads to execute in order by ID.
            thread.join();
            thread.writeToFile();
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
}
