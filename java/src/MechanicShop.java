/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.management.RuntimeErrorException;

import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest"); //same as 1-3?
				System.out.println("5. CloseServiceRequest"); //same as 1-3?
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddCustomer(MechanicShop esql){//1
		int ID;
		String Fname;
		String Lname;
		String Phone;
		String Address;
		//Grabbing Customer ID 
		do{
			System.out.println("Input Customer ID Number: ");
			try{
				ID = Integer.parseInt(in.readLine());
				String query = "SELECT C.id\nFROM Customer C\nWHERE EXISTS (SELECT C.id FROM CUSTOMER  WHERE C.id = " + ID +");";
				List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
				if(Query_Results.size() != 0){
					System.out.println("Customer ID Number already EXISTS:");
				}
				else{
					break;
				}	
			}

			catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		//Grabbing Customer Fname
		do{
			System.out.println("Input Customer First Name: ");
			try{
				Fname  = in.readLine();
				if(Fname.length() <= 0 || Fname.length() > 32){
					throw new RuntimeException("Fname greater than 32 or empty");
				}
				break;
			}
			catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);
		//Grabbing Customer Lname
		do{
			System.out.println("Input Customer Last Name: ");
			try{
				Lname  = in.readLine();
				if(Lname.length() <= 0 || Lname.length() > 32){
					throw new RuntimeException("Lname greater than 32 or empty");
				}
				break;
			}
			catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);
		//Grabbing Customer Phone Number 
		do{
			System.out.println("Input Customer Phone Number: ");
			try{
				Phone  = in.readLine();
				if(Phone.length() <= 0 || Phone.length() > 13){
					throw new RuntimeException("Phone Number greater than 13 or empty");
				}
				break;
			}
			catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);
		//Grabbing Customer Address
		do{
			System.out.println("Input Customer Address: ");
			try{
				Address  = in.readLine();
				if(Address.length() <= 0 || Address.length() > 256){
					throw new RuntimeException("Address greater than 256 or empty");
				}
				break;
			}
			catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		try{
			String query = "INSERT INTO Customer(id, fname, lname, phone, address) VALUES ( "+ ID + ",\'" + Fname + "\',\'" + Lname + "\',\'"  + Phone + "\',\'" + Address +  "\');";
			esql.executeUpdate(query);
			System.out.println("RESULT________________________________________________");
			String Output_query = "SELECT * \n FROM Customer C\n WHERE C.id =" + ID  +";"; 
			int result = esql.executeQueryAndPrintResult(Output_query);
			System.out.println(result);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2
	
		int ID;
		String Fname;
		String Lname;
		int Experience;
		//Grabbing Mechanic ID 
		do{
			System.out.println("Input Mechanic ID Number: ");
			try{
				ID = Integer.parseInt(in.readLine());
				String query = "SELECT M.id\nFROM Mechanic M\nWHERE EXISTS (SELECT M.id FROM Mechanic WHERE M.id = " + ID +");";
				List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
				if(Query_Results.size() != 0){
					System.out.println("Mechanic ID Number already EXISTS:");
				}
				else{
					break;
				}	
			}
			catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);
		//Grabbing Mechanic Fname
		do{
			System.out.println("Input Mechanic First Name: ");
			try{
				Fname  = in.readLine();
				if(Fname.length() <= 0 || Fname.length() > 32){
					System.out.println(("Enter vaild Fname length "));
				}
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				System.out.println(e);
				continue;
			}
		}while(true);
		//Grabbing Mechanid Lname
		do{
			System.out.println("Input Mechanic Last Name: ");
			try{
				Lname  = in.readLine();
				if(Lname.length() <= 0 || Lname.length() > 32){
					System.out.println(("Enter vaild Lname length "));
				}
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}
		}while(true);
		//Grabbing Mechanic experience 
		do{
			System.out.println(("Input Mechanic Experience: "));
			try{
				Experience = Integer.parseInt(in.readLine());
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}
		}while(true);


		try{
			String query = "INSERT INTO Mechanic(id, fname, lname, experience) VALUES ( "+ ID + ",\'" + Fname + "\',\'" + Lname+ "\',\'"  + Experience +  "\');";
			esql.executeUpdate(query);
			// esql.executeQueryAndPrintResult(query)
			// System.out.println(esql.executeQuery(query));
			System.out.println("RESULT________________________________________________");
			String Output_query = "SELECT * \n FROM Mechanic M\n WHERE M.id =" + ID  +";"; 
			int result = esql.executeQueryAndPrintResult(Output_query);
			System.out.println(result);

         	
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		
		
	}
	
	public static void AddCar(MechanicShop esql){//3
		
		String Vin;
		String Make;
		String Model;
		String Year;
		//Grabbing Vehicle vin 
		do{
			System.out.println("Input Vehicle Vin: ");
			try{
				Vin = in.readLine();
				if(Vin.length() == 0 || Vin.length() > 16){
					throw new RuntimeException("Vin greater than 16 or empty");
				}
				String query = "SELECT C.vin\nFROM Car C\nWHERE EXISTS (SELECT C.vin FROM Car WHERE C.vin = '" + Vin +"');";
				List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
				if(Query_Results.size() != 0){
					System.out.println("Vin Number already EXISTS:");
				}
				else{
					break;
				}	
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}
		}while(true);

		//Grabbing Vehicle Make 
		do{
			System.out.println("Input Vehicle Make: ");
			try{
				Make = in.readLine();
				if(Make.length() == 0 || Make.length() > 32){
					throw new RuntimeException("Make greater than 32 or empty");
				}
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}
		}while(true);

		//Grabbing Vehicle Model
		do{
			System.out.println(("Input Vehicle Model: "));
			try{
				Model = in.readLine();
				if(Model.length() == 0 || Model.length() > 32){
					throw new RuntimeException("Model greater than 32 or empty");
				}
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}
		}while(true);

		//Grabbing Vehicle Year
		do{
			System.out.println(("Input Vehicle Year: "));
			try{
				Year = in.readLine();
				//setting up length 
				if(Year.length() != 4 || Year.length() == 0){
					throw new RuntimeException("Year is not vaild!");
				}
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}
		}while(true);


		try{
			String query = "INSERT INTO Car(vin, make, model, year) VALUES ( '"+ Vin +"',\'" + Make + "\',\'" + Model+ "\',\'"  + Year +  "\');";
			esql.executeUpdate(query);
			// System.out.println(esql.executeQuery(query));
			System.out.println("RESULT________________________________________________");
			String Output_query = "SELECT * \n FROM Car C\n WHERE C.vin = '" + Vin  +"';"; 
			int result = esql.executeQueryAndPrintResult(Output_query);
			System.out.println(result);
		
         	
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		
	}
	public static int Menu_Option(List<List<String>> query) throws SQLException{
		int return_index;
		int row_index = 1;
		System.out.println(query.size());
		// if(query.size() >= 0){
			for(int i = 0; i < query.size(); ++i){
				System.out.println(row_index + ": " + query.get(i));
				row_index += 1;
			}
			try{
				return_index = Integer.parseInt(in.readLine());
				return_index -= 1;
				return return_index;

			}
			catch(Exception e){
				System.err.println("Input is invalid");

			}
		// }
		// else{
			// return 1;
		// }
		return 0;
	}
	//ADD AN UPDATE FEATURE 
	public static void InsertServiceRequest(MechanicShop esql){//4
		//1) get customer lname
		String Lname;
		int CID; 
		String C_vin;
		int input;
		String Car_vin;
		int RID;
		int Odometer;
		String Date;
		String Complain;


		do{
		System.out.println("Input Customer's Last Name: ");			
			try{
				Lname  = in.readLine();
				if(Lname.length() <= 0 || Lname.length() > 32){
					throw new RuntimeException("Lname greater than 32 or empty");
				}
				break;
		
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
		
			}
		}while(true);


		//2) grab customer serivce id -> customer.id 
		System.out.println("Grabbing Customer Information ...... ");
	
		try{
	   		String query = "SELECT C.lname, C.fname, C.id FROM Customer C WHERE C.lname = '" + Lname + "' ;";
			int rowCount = esql.executeQuery(query);
		   if(rowCount != 0){
			   //3) list customers with lname
				List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
				int item = Menu_Option(Query_Results);
				System.out.println(Query_Results.get(item));
				String C_id = Query_Results.get(item).get(2);
				System.out.println("CUSTOMER ID: " + C_id);
		   }
		   else{
			//4) If customer does not exisit create new entry
			System.out.println("Customer is not in Database! Please create new Customer ...... ");
			   AddCustomer(esql);
		   }
		
		}
		catch(Exception e){
		 System.err.println(e.getMessage());
	
		}

		
		
		do{
			System.out.println("Enter Customer ID from screen");
			try{
				CID = Integer.parseInt(in.readLine());
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}

		}while(true);
		
		System.out.println("ARE YOU ADDING A NEW CAR (yes(1)/no(0))");
		try{
			input = Integer.parseInt(in.readLine());

			if(input == 1){
			//4) If customer does not exisit create new entry
				System.out.println("Please add Car ...... ");
				AddCar(esql);

			}
			else{
			
				try{
					System.out.println("Select Car ");
					String query = "SELECT C.make, C.model, C.vin\nFROM Car C, Owns O \nWHERE C.vin = O.car_vin AND O.customer_id = " + CID + ";";
					int rowCount = esql.executeQuery(query);
					if(rowCount != 0){
						//5) list all the cars that customer owns
						List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
						int item = Menu_Option(Query_Results);
						System.out.println(Query_Results.get(item));
						C_vin = Query_Results.get(item).get(2);
						System.out.println("CAR VIN: " + C_vin);
					}
				}
				catch(Exception e){
					System.err.println(e.getMessage());
				}
			}

		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

		do{
			System.out.println("Enter Car Vin from screen");
			try{
				Car_vin = in.readLine();
				break;
			}
			catch(Exception e){
				System.err.println("Input is invalid");
				continue;
			}

		}while(true);

		System.out.println("PRESS '0' FOR UPDATING SERVICE REQUEST:\nPRESS '1' FOR NEW SERVICE REQUEST: ");
		try{
			int input_s = Integer.parseInt(in.readLine());
			if(input_s == 0){
				System.out.println("OLD SERVICE REQUEST__________________");
				String tmp_query = "SELECT *\nFROM Service_Request SR\n WHERE SR.customer_id = " +CID+ " AND SR.car_vin = '"+ Car_vin + "';";
				int result = esql.executeQueryAndPrintResult(tmp_query);
				//6) update service request for that car 
				if(result != 0){
					// System.out.println(result);
					do{
						System.out.println("Enter Service Request ID:");
						try{
							RID = Integer.parseInt(in.readLine());
							break;
						}
						catch(Exception e){
							System.err.println("Input is invalid");
							continue;
						}
	
					}while(true);
	
					do{
						System.out.println("Enter ODOMETER RADING:");
						try{
							Odometer = Integer.parseInt(in.readLine());
							break;
						}
						catch(Exception e){
							System.err.println("Input is invalid");
							continue;
						}
						
			
					}while(true);
	
					do{
						System.out.println("Enter DATE AS (mm/dd/yyyy):");
						try{
							Date = in.readLine();
							Date += " 00:00";
							break;
						}
						catch(Exception e){
							System.err.println("Input is invalid");
							continue;
						}
					}while(true);
	
					do{
						System.out.println("Enter Complain:");
						try{
							Complain = in.readLine();
							break;
						}
						catch(Exception e){
							System.err.println("Input is invalid");
							continue;
						}
					}while(true);

					try{
                        //String u_query = "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain ) VALUES ("+ RID + ",\'" + CID + "\',\'" + Car_vin+ "\',\'"  + Date +  "\',\'" + Odometer + "\',\'" + Complain + "\'); ";
                        String u_query = "UPDATE Service_Request  \nSET date = '"+ Date + "' ,odometer = "+ Odometer +", complain = '" + Complain +"'\nWHERE rid = " + RID + ";";
                        //esql.executeUpdate(u_query);
                        String old_query = "SELECT *\nFROM Service_Request SR\nWHERE SR.rid = " + RID + ";";
                        System.out.println("OLD SERVICE REQUEST:_________________");
                        int old_result = esql.executeQueryAndPrintResult(old_query);
                        esql.executeUpdate(u_query);
                        System.out.println("UPDATE RESULT_______________");
        	            //int new_result = esql.executeQueryAndPrintResult(u_query);
                        String update_query = "SELECT *\nFROM Service_Request SR\nWHERE SR.rid = " + RID + ";";
                        int new_result = esql.executeQueryAndPrintResult(update_query);

					}
					catch(Exception e){
						System.err.println("Input is invalid");
						
					}

				}
				else{
					System.out.println("Service Request does not exisit.......");
				}

			}
			//7) if its a new car create a new serivce request for exisiting customer
			else{

				do{
					System.out.println("Enter Service Request ID:");
					try{
						RID = Integer.parseInt(in.readLine());
						String query = "SELECT SR.rid\nFROM Service_Request SR\nWHERE EXISTS (SELECT SR.rid FROM Service_Request WHERE SR.rid = " + RID +");";
						List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
						if(Query_Results.size() != 0){
							System.out.println("Serivce_Request already EXISTS:");
						}
						else{
							break;
						}
					}
					catch(Exception e){
						System.err.println("Input is invalid");
						continue;
					}

				}while(true);

				do{
					System.out.println("Enter ODOMETER RADING:");
					try{
						Odometer = Integer.parseInt(in.readLine());
						break;
					}
					catch(Exception e){
						System.err.println("Input is invalid");
						continue;
					}
					
		
				}while(true);

				do{
					System.out.println("Enter DATE AS (mm/dd/yyyy):");
					try{
						Date = in.readLine();
						Date += " 00:00";
						break;
					}
					catch(Exception e){
						System.err.println("Input is invalid");
						continue;
					}
				}while(true);

				do{
					System.out.println("Enter Complain:");
					try{
						Complain = in.readLine();
						break;
					}
					catch(Exception e){
						System.err.println("Input is invalid");
						continue;
					}
				}while(true);
				try{
					String u_query = "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain ) VALUES ("+ RID + ",\'" + CID + "\',\'" + Car_vin+ "\',\'"  + Date +  "\',\'" + Odometer + "\',\'" + Complain + "\'); ";
					esql.executeUpdate(u_query);
					System.out.println("RESULT:______________");
					String update_query = "SELECT *\nFROM Service_Request SR\nWHERE SR.rid = " + RID + ";";
                    int new_result = esql.executeQueryAndPrintResult(update_query);

	
				}
				catch(Exception e){
					System.err.println("Input is invalid");
					
				}

			}
			
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}


	}
	//Update maybe?
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		int Mid;
		int Rid;
		int WID;
		int bill;
		String CR_rid;
		String Comment;
		



		System.out.println("PLEASE ENTER MECHANIC ID:_______");
		do{
			try{
				Mid = Integer.parseInt(in.readLine());
				break;

			}
			catch(Exception e){
				System.err.println(e.getMessage());
				continue;
			}

		}while(true);

		try{
			String query = "SELECT CR.rid, CR.mid\nFROM Closed_Request CR\nWHERE EXISTS\n(SELECT M.id\nFROM Mechanic M\nWHERE M.id = " + Mid + "AND CR.mid = "+Mid+");";
			List<List<String>> Query_Results = esql.executeQueryAndReturnResult(query);
			int item = Menu_Option(Query_Results);
			System.out.println("PLEASE SELECT SERVICE REQUEST ID:");
			// System.out.println(Query_Results.get(item));
			CR_rid = Query_Results.get(item).get(0);
			System.out.println("Service Request: " + CR_rid);
			if(item == 0){
				System.out.println("MECHANIC ID DOES NOT EXISTS");
			}

		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

		do{
			System.out.println("ENTER SERVICE REQUEST FROM SCREEN:____");
			try{
				Rid = Integer.parseInt(in.readLine());
				break;
			}
			catch(Exception e){
				System.err.println(e.getMessage());
				continue;
			}
		}while(true);

		try{
			String query = "SELECT * \nFROM Service_Request SR\nWHERE SR.rid = " + Rid + ";";
			int new_result = esql.executeQueryAndPrintResult(query);
			
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}


		System.out.println("UPDATE SERVICE INTO A CLOSED_REQUEST");
		try{
			String query = "SELECT CR.wid \nFROM Closed_Request CR\nWHERE CR.rid = " + Rid + "AND CR.mid = " +Mid +";";
			System.out.println("CLOSED REQUEST ID:");
			int new_result = esql.executeQueryAndPrintResult(query);
			
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

		System.out.println("PLEASE ENTER CLOSED REQUEST ID:_______");
		do{
			try{
				WID = Integer.parseInt(in.readLine());
				break;

			}
			catch(Exception e){
				System.err.println(e.getMessage());
				continue;
			}

		}while(true);

		try{
			String query = "SELECT * \nFROM Closed_Request CR\nWHERE CR.wid = " + WID +";";
			int new_result = esql.executeQueryAndPrintResult(query);
			
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}

		System.out.println("PLEASE UPDATE COMMENT");
		do{
			try{
				Comment = in.readLine();
				break;

			}
			catch(Exception e){
				System.err.println(e.getMessage());
				continue;
			}

		}while(true);

		System.out.println("PLEASE UPDATE BILL:_______");
		do{
			try{
				bill = Integer.parseInt(in.readLine());
				break;

			}
			catch(Exception e){
				System.err.println(e.getMessage());
				continue;
			}

		}while(true);



		try{
			// String u_query = "INSERT INTO Closed_Request ;";
			String u_query = "UPDATE Closed_Request  \nSET date = CURRENT_DATE , bill = " + bill  +",comment = '" + Comment +"'\nWHERE wid = " + WID + ";";
			//esql.executeUpdate(u_query);
			String old_query = "SELECT *\nFROM Closed_Request CR\nWHERE CR.wid = " + WID + ";";
			System.out.println("OLD CLOSED REQUEST:_________________");
			int old_result = esql.executeQueryAndPrintResult(old_query);
			esql.executeUpdate(u_query);
			System.out.println("UPDATE RESULT_______________");
			//int new_result = esql.executeQueryAndPrintResult(u_query);
			String update_query = "SELECT *\nFROM Closed_Request CR\nWHERE CR.wid = " + WID + ";";
			int new_result = esql.executeQueryAndPrintResult(update_query);

		}
		catch(Exception e){
			System.err.println(e.getMessage());
			
		}
	

	}


	//DONE Works Fine 
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		//List customer C_fname C_lname CR_bill
		//Select C_fname C_lname CR_bill
		//From  Customer C Closed_Request CR Service_Request SR
		//Where CR_bill <= 100 CR_rid = SR_rid AND SR_customer_id = C_id
		//Order By CR_bill DESC
		try{
			String query = "SELECT DISTINCT  CR.date, C.fname, C.lname, CR.bill,CR.comment\nFROM Customer C, Closed_Request CR, Service_Request SR\n WHERE CR.bill <= 100 AND  CR.rid = SR.rid AND SR.customer_id = C.id\nORDER BY CR.bill DESC;";
			// String query = "SELECT DISTINCT C.fname, C.lname, CR.bill\nFROM Customer C, Closed_Request CR, Service_Request SR\n WHERE CR.bill <= 100 AND  CR.rid = SR.rid AND SR.customer_id = C.id\nORDER BY CR.bill DESC;";
			// if(esql.executeQueryAndPrintResult(query) == 0){
					// System.out.println("Query does not exist");
			// }
			// else{
					//List<List<String>> List_Res  = esql.executeQueryAndReturnResult(query);
					//System.out.println(esql.executeQuery(query));
					int result = esql.executeQueryAndPrintResult(query);
					// System.out.println(result);
			// }
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		
	}
	//DONE Works Fine
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		//List C.fname C.lname Count(O.ownership_id)
		//Select C.fname C.lname 
		//From Owns O Customer C 
		//Where 20 < (Select Count(O.ownership_id)
		//			  From Owns O Customer CC
		//			  Where O.customer_id = CC.id)
		try{
			String query = "SELECT C.fname, C.lname\nFROM Customer C\nWHERE 20 < ( SELECT COUNT(O.customer_id)\nFROM Owns O\nWHERE O.customer_id = C.id);";
			//String query = "SELECT C.fname, C.lname\n FROM Customer C\n WHERE 20 < (SELECT COUNT(O.ownership_id)\n FROM Owns O, Customer CC\n WHERE O.customer_id = CC.id); ";
			// String query = "SELECT C.fname, C.lname, COUNT(O.ownership_id) AS count\n FROM Owns O, Customer C\n WHERE 20 < (SELECT count\n FROM Owns OO, Customer CC\n WHERE OO.customer_id = CC.id); ";
			// if(esql.executeQueryAndPrintResult(query) == 0){
				// System.out.println("Query does not exist");
			// }
			// else{
				//System.out.println(esql.executeQuery(query));
				int result = esql.executeQueryAndPrintResult(query);
				// System.out.println(result);
			// }
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}


		
	}
	//THURSDAY QUESTION 
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		

		try{
			String query = "SELECT C.make, C.model, C.year\nFROM Car C, Service_Request SR\nWHERE C.year < 1995 AND SR.odometer < 50000 AND C.vin = SR.car_vin ;";
	
			// if(esql.executeQueryAndPrintResult(query) == 0){
				// System.out.println("Query does not exist");
			// }
			// else{
				//System.out.println(esql.executeQuery(query));
				int result = esql.executeQueryAndPrintResult(query);
				// System.out.println(result);
			// }
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		int K_value;
		do{
			try{
				System.out.println("List the number of K cars ________: " );
				K_value = Integer.parseInt(in.readLine());
				if(K_value  <= 0){
					throw new RuntimeException("Integer K most be greter than 0");
				}
				else{
					break;
				}

			}
			catch(Exception e){
				System.err.println(e.getMessage());
				continue;
			}

		}while(true);
		try{
			String query = "SELECT C.make, C.model, COUNT(SR.rid) AS service\nFROM Car C, Service_Request SR\nWHERE C.vin = SR.car_vin\nGROUP BY C.make, C.model\nORDER BY service DESC LIMIT "  + K_value + ";";
			// if(esql.executeQueryAndPrintResult(query) == 0){
				// System.out.println("Query does not exist");
			// }
			// else{
				//System.out.println(esql.executeQuery(query));
				int result = esql.executeQueryAndPrintResult(query);
				// System.out.println(result);
			// }
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		//
		//Select C.fname, C.lname, Count(CR.bill) As total
		//From Customer C, Closed_Request CR, Service_Request SR, Owns O
		//Where C.id = O.customer_id AND SR.vin = O.car_vin AND SR.rid = CR.rid 
		//Group by C.fname, C.lname
		//Order by total Desc 
		try{
			String query = "SELECT C.fname, C.lname, SUM(CR.bill) AS total\nFROM Customer C, Closed_Request CR, Service_Request SR, Owns O\nWHERE C.id = O.customer_id AND SR.car_vin = O.car_vin AND SR.rid = CR.rid\nGROUP BY C.fname, C.lname\nORDER BY total DESC;";
			// if(esql.executeQueryAndPrintResult(query) == 0){
				// System.out.println("Query does not exist");
			// }
			// else{
				//System.out.println(esql.executeQuery(query));
				int result = esql.executeQueryAndPrintResult(query);
				// System.out.println(result);
			// }
		}

		catch(Exception e){
			System.err.println(e.getMessage());
		}
		
	}	
}
