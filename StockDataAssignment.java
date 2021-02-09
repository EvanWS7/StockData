/* 
Stock Data Program that communicates with the WWU database and allows the user to receive specific ticker information according to inputs.
*/

import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.PrintWriter;

class SaundersAssignment2 {
   
   static class StockData {	   
	   // To Do: 
	   // Create this class which should contain the information  (date, open price, high price, low price, close price) for a particular ticker  
	   String date;
	   double openPrice;
	   double highPrice;
	   double lowPrice;
	   double closePrice;
	   
   }
   
   static Connection conn;
   static final String prompt = "Enter ticker symbol [start/end dates]: ";
private static final String SaundersAssignment2 = null;
   
   public static void main(String[] args) throws Exception {
      String paramsFile = "readerparams.txt";
      if (args.length >= 1) {
         paramsFile = args[0];
      }
      
      Properties connectprops = new Properties();
      connectprops.load(new FileInputStream(paramsFile));
      try {
         Class.forName("com.mysql.jdbc.Driver");
         String dburl = connectprops.getProperty("dburl");
         String username = connectprops.getProperty("user");
         conn = DriverManager.getConnection(dburl, connectprops);
         System.out.printf("Database connection %s %s established.%n", dburl, username);
         
         Scanner in = new Scanner(System.in);
         System.out.print("Enter a ticker symbol [start/end dates]: ");
         String input = in.nextLine().trim();
         
         while (input.length() > 0) {
            String[] params = input.split("\\s+");
            String ticker = params[0];
            String startdate = null, enddate = null;
            
            if (getName(ticker)) {
               if (params.length >= 3) {
                  startdate = params[1];
                  enddate = params[2];
               }               
               Deque<String> data = getStockData(ticker, startdate, enddate);
               System.out.println();
               System.out.println("Executing investment strategy");
               doStrategy(ticker, data);
            } 
            
            System.out.println();
            System.out.print("Enter a ticker symbol [start/end dates]: ");
            input = in.nextLine().trim();
         }

         // Close the database connection
        conn.close();
      } catch (SQLException ex) {
         System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                           ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
      }
   }
   
   static boolean getName(String ticker) throws SQLException {
	  	  // To Do: 
	  	  // Execute the first query and print the company name of the ticker user provided (e.g., INTC to Intel Corp.) 
	         PreparedStatement stmt = conn.prepareStatement("select Name " + " from company " + " where Ticker = ? ");
	         stmt.setString(1, ticker);
	         ResultSet rs = stmt.executeQuery();
	         
	         if (rs.next()) {
	             System.out.println(rs.getString(1));
	         } else {
	             System.out.printf("Ticker %s not found.%n", ticker);
	         }
	         stmt.close();
	  	return true;
	   }
       
       

   static Deque<String> getStockData(String ticker, String start, String end) throws SQLException{	  
	// Executes second query with getStockData values
	PreparedStatement pstmt = conn.prepareStatement(
	               "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
	               "  from pricevolume " +
	               "  where Ticker = ? and TransDate between ? and ?" + "order by TransDate DESC");
	   pstmt.setString(1, ticker);
	   pstmt.setString(2, start);
	   pstmt.setString(3, end);
	   ResultSet rs = pstmt.executeQuery();
	       
	   //create deque array
       Deque<String> result = new ArrayDeque<>();
       

       
       //stock variables
       StockData updated = new StockData();
       double previousClose = 0;
       int day = 0;
       int stockSplit = 0;
       double divisor = 1.0;
       String rDate = "";
       double rOpen = 0;
       double rHigh = 0;
       double rLow = 0;
       double rClose = 0;
       //gets stock splits for ticker between two dates
       while(rs.next()) {
    	   day++;
    	   updated.date = rs.getString(1);
    	   updated.openPrice =  Double.parseDouble(rs.getString(2));
    	   updated.highPrice =  Double.parseDouble(rs.getString(3));
    	   updated.lowPrice =  Double.parseDouble(rs.getString(4));
    	   updated.closePrice = Double.parseDouble(rs.getString(5));
    	   rDate = updated.date;
    	   rOpen = updated.openPrice/divisor;
    	   rHigh = updated.highPrice/divisor;
    	   rLow = updated.lowPrice/divisor;
    	   rClose = updated.closePrice/divisor;
    	// Storing data from SQL into array
    	   result.addFirst(Double.toString(rClose));
    	   result.addFirst(Double.toString(rLow));
    	   result.addFirst(Double.toString(rHigh));
    	   result.addFirst(Double.toString(rOpen));
    	   result.addFirst(rDate);
    	   
       
    	   
    	   if((Math.abs((updated.openPrice/previousClose)-2.0)) < 0.20){
				System.out.println("A 2:1 split on " + updated.date + "\t" + updated.closePrice + " --> " + previousClose);
				stockSplit++;
				divisor *= 2;
    	   }
    	   if((Math.abs((updated.openPrice/previousClose)-3.0)) < 0.30){
    		   System.out.println("A 3:1 split on " + updated.date + "\t" + updated.closePrice + " --> " + previousClose);
				stockSplit++;
				divisor *= 3;
    	   }
    	   if((Math.abs((updated.openPrice/previousClose)-1.5)) < .15){
				System.out.println("A 3:2 split on " + updated.date + "\t" + previousClose + " --> " + updated.openPrice);
				stockSplit++;
				divisor *= 1.5;
				
    	   }
    	   previousClose = updated.openPrice;
       }
       System.out.println(stockSplit + " splits in " + day + " trading days");

       pstmt.close();
       return result;
      
   }
}
