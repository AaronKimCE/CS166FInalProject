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
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
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
		while(rs.next()){
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
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
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
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
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

	public static void AddDoctor(DBproject esql) {//1
		// Add a doctor to the database
		int did;
		String dname;
		String Specialty;
		int deptid;

		do { // ID
			System.out.print("Input Doctor's ID:");
			try {
				did = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Name
			System.out.print("Input Doctor's Name:");
			try {
				dname = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Specialty
			System.out.print("Input Doctor's Specialty:");
			try {
				Specialty = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // DID
			System.out.print("Input Doctor's Department ID:");
			try {
				deptid = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES (" + did + ", \'" + dname + "\', \'" + Specialty + "\', " + deptid + ");";
			esql.executeUpdate(query);
			System.out.println("Doctor added.");
		} catch (Exception e) {
			System.out.println("Table update error! Please double check values!");
		}
		return;
	}
	
	public static void AddPatient(DBproject esql) {//2
	// Add a patient to the database
		int pid;
		String pname;
		String gender;
		int age;
		String address;
		int prevn;

		do { // ID
			System.out.print("Input Patient's ID:");
			try {
				pid = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Name
			System.out.print("Input Patient's name:");
			try {
				pname = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Gender
			System.out.print("Input Patient's Gender (M, F):");
			try {
				gender = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // age
			System.out.print("Input Patient's age:");
			try {
				age = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Address
			System.out.print("Input Patient's address:");
			try {
				address = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Num Appts
			System.out.print("Input Patient's Number of Previous Appointments:");
			try {
				prevn = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (" + pid + ", \'" + pname + "\', \'" + gender + "\', " + age + ", \'" + address + "\', " + prevn + ");";
			esql.executeUpdate(query);
			System.out.println("Patient added.");
		} catch (Exception e) {
			System.out.println("Table update error! Please double check values!");
		}
		return;
	}

	public static void AddAppointment(DBproject esql) {//3
		// Add an appointment to the database
		int aid;
		String date;
		String timeslot;
		String status;

		do { // ID
			System.out.print("Input Appointment's ID:");
			try {
				aid = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // date
			System.out.print("Input Appointment's Date (YYYY-MM-DD):");
			try {
				date = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // timeslot
			System.out.print("Input Appointment's Timeslot (HH:MM-HH:MM):");
			try {
				timeslot = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // status
			System.out.print("Input Appointment's Status (PA, AC, AV, WL):");
			try {
				status = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "INSERT INTO Appointment (appnt_ID, adate, time_slot, status) VALUES (" + aid + ", \'" + date + "\', \'" + timeslot + "\', \'" + status + "\');";
			esql.executeUpdate(query);
			System.out.println("Appointment added.");
		} catch (Exception e) {
			System.out.println("Table update error! Please double check values!");
		}
		return;
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		// Search for patient
		int pid;
		String pname;
		String gender;
		int age;
		String address;
		int prevn;
		int rs1 = 0;

		do { // ID
			System.out.print("Input Patient's ID:");
			try {
				pid = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Name
			System.out.print("Input Patient's name:");
			try {
				pname = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Gender
			System.out.print("Input Patient's Gender (M, F):");
			try {
				gender = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // age
			System.out.print("Input Patient's age:");
			try {
				age = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Address
			System.out.print("Input Patient's address:");
			try {
				address = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Num Appts
			System.out.print("Input Patient's Number of Previous Appointments:");
			try {
				prevn = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "SELECT patient_ID FROM Patient WHERE patient_ID = " + pid + ";";
			rs1 = esql.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Search Table Error! Please double check values!");
		}
		if (rs1 == 0) { // Didn't find patient must update
			System.out.println("Patient was not found in database, attempting to add new patient...");
			try { // Run the query
				String query = "INSERT INTO Patient (patient_ID, name, gtype, age, address, number_of_appts) VALUES (" + pid + ", \'" + pname + "\', \'" + gender + "\', " + age + ", \'" + address + "\', " + prevn + ");";
				esql.executeUpdate(query);
				System.out.println("Patient added.");
			} catch (Exception e) {
				System.out.println("Table update error! Please double check values!");
			}
		} else { // Found the patient
			System.out.println("Patient found.");
		}

		// Search for doctor
		int did;
		String dname;
		String Specialty;
		int deptid;
		int rs2 = 0;

		do { // ID
			System.out.print("Input Doctor's ID:");
			try {
				did = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Name
			System.out.print("Input Doctor's Name:");
			try {
				dname = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Specialty
			System.out.print("Input Doctor's Specialty:");
			try {
				Specialty = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // DID
			System.out.print("Input Doctor's Department ID:");
			try {
				deptid = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "SELECT doctor_ID FROM Doctor WHERE doctor_ID = " + did + ";";
			rs2 = esql.executeQuery(query);
		} catch (Exception e) {
			System.out.println("Search Table Error! Please double check values!");
		}
		if (rs2 == 0) { // Didn't find doctor must update
			System.out.println("Doctor was not found in database, attempting to add new doctor...");
			try { // Run the query
				String query = "INSERT INTO Doctor (doctor_ID, name, specialty, did) VALUES (" + did + ", \'" + dname + "\', \'" + Specialty + "\', " + deptid + ");";
				esql.executeUpdate(query);
				System.out.println("Doctor added.");
			} catch (Exception e) {
				System.out.println("Table update error! Please double check values!");
			}
		} else { // Found the patient
			System.out.println("Doctor found.");
		}

		// Search for appointment
		int aid;
		String date;
		String timeslot;
		String status;
		List<List<String>> rs3 = new ArrayList<List<String>>();
		List<String> rs3b = new ArrayList<String>();
		rs3b.add("");
		rs3.add(rs3b);

		do { // ID
			System.out.print("Input Appointment's ID:");
			try {
				aid = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // date
			System.out.print("Input Appointment's Date (YYYY-MM-DD):");
			try {
				date = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // timeslot
			System.out.print("Input Appointment's Timeslot (HH:MM-HH:MM):");
			try {
				timeslot = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // status
			System.out.print("Input Appointment's Status (PA, AC, AV, WL):");
			try {
				status = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "SELECT status FROM Appointment WHERE appnt_ID = " + aid + ";";
			rs3 = esql.executeQueryAndReturnResult(query);
		} catch (Exception e) {
			System.out.println("Search Table Error! Please double check values!");
		}
		if (rs3.size() == 0) { // Didn't find appointment must update
			System.out.println("Appointment was not found in database, attempting to add new appointment...");
			try { // Run the query
				String query = "INSERT INTO Appointment (appnt_ID, adate, time_slot, status) VALUES (" + aid + ", \'" + date + "\', \'" + timeslot + "\', \'" + status + "\');";
				esql.executeUpdate(query);
				System.out.println("Appointment added.");
			} catch (Exception e) {
				System.out.println("Table update error! Please double check values!");
			}
		} else { // Found the appointment
			System.out.println("Appointment found.");
		}
		// Check the appointment status & update
		if (rs3.get(0).get(0).equals("PA")) { // Past appointment (Not available);
			System.out.println("Appointment already concluded. Not available.");
		} else if (rs3.get(0).get(0).equals("AC")) { // Appointment already active, change to waitlisted and update tuples
			try { // Run the query
				String query = "UPDATE Appointment SET status = \'WL\' WHERE appnt_ID = " + aid + ";"; // UPDATE appointment to WL
				esql.executeUpdate(query);
				query = "UPDATE Patient SET number_of_appts = " + (prevn + 1) + " WHERE patient_ID = " + pid + ";"; // UPDATE number appnts
				esql.executeUpdate(query);
				// Adding appointment to has_appointment table
				query = "INSERT INTO has_appointment (appt_id, doctor_id) VALUES (" + aid + ", " + did + ");";
				esql.executeUpdate(query);
				System.out.println("Appointment already booked. Added to waitlist.");
			} catch (Exception e) {
				System.out.println("Table update error! Please double check values!");
			}
		} else if (rs3.get(0).get(0).equals("AV")) { // Appointment is available, chenge to active and update tuples
			try { // Run the query
				String query = "UPDATE Appointment SET status = \'AC\' WHERE appnt_ID = " + aid + ";"; // UPDATE appointment to AC
				esql.executeUpdate(query);
				query = "UPDATE Patient SET number_of_appts = " + (prevn + 1) + " WHERE patient_ID = " + pid + ";"; // UPDATE number appnts
				esql.executeUpdate(query);

				// Adding appointment to has_appointment table
				query = "INSERT INTO has_appointment (appt_id, doctor_id) VALUES (" + aid + ", " + did + ");";
				esql.executeUpdate(query);
				System.out.println("Appointment booked. Thank you.");
			} catch (Exception e) {
				System.out.println("Table update error! Please double check values!");
			}
		} else if (rs3.get(0).get(0).equals("WL")) { // Appointment is waitlisted, update tuples
			try { // Run the query
				String query = "UPDATE Patient SET number_of_appts = " + (prevn + 1) + " WHERE patient_ID = " + pid + ";"; // UPDATE number appnts
				esql.executeUpdate(query);

				// Adding appointment to has_appointment table
				query = "INSERT INTO has_appointment (appt_id, doctor_id) VALUES (" + aid + ", " + did + ");";
				esql.executeUpdate(query);
				System.out.println("Appointment currently waitlisted. Added to waitlist.");
			} catch (Exception e) {
				System.out.println("Table update error! Please double check values!");
			}
		} else {
			System.out.println("Unknown Appointment Status.");
			System.out.println(rs3.get(0).get(0));
		}

		return;
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		int did;
		String startdate;
		String enddate;

		do { // ID
			System.out.print("Input Doctor's ID:");
			try {
				did = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // startdate
			System.out.print("Starting from what date? (YYYY-MM-DD):");
			try {
				startdate = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // enddate
			System.out.print("Ending on what date? (YYYY-MM-DD):");
			try {
				enddate = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "SELECT A.appnt_ID, A.status FROM Appointment A, Doctor D, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND (A.status = 'AC' OR A.status = 'AV') AND D.doctor_ID = " + did + " AND A.adate >= DATE(\'" + startdate + "\') AND A.adate <= DATE(\'" + enddate + "\');";
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error! Please double check values!");
		}

		return;
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		String dname;
		String date;

		do { // Name
			System.out.print("Input Department's Name:");
			try {
				dname = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		do { // Date of appointment
			System.out.print("Input date of appointments (YYYY-MM-DD):");
			try {
				date = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "SELECT A.appnt_ID, A.time_slot FROM Appointment A, Doctor D, Department De, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND D.did = De.dept_ID AND A.status = 'AV' AND A.adate = DATE(\'" + date + "\') AND De.name = \'" + dname + "\';";
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error! Please double check values!");
		}

		return;
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		List<List<String>> rs3 = new ArrayList<List<String>>();
		List<String> rs3b = new ArrayList<String>();
		rs3b.add("");
		rs3.add(rs3b);

		try { // Get the max value of doctorID
			String query = "SELECT MAX(D.doctor_ID) FROM Doctor D;";
			rs3 = esql.executeQueryAndReturnResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error!!");
		}

		int arraySize = Integer.parseInt(rs3.get(0).get(0));
		List<List<String>> Container = new ArrayList<List<String>>(); // Initialize container for values
		for (int i = 0; i <= arraySize; i++) {
			List<String> index = new ArrayList<String>();
			index.add("" + i);
			for (int j = 0; j < 4; j++) {
				index.add("0");
			}
			Container.add(index);
		}

		try { // Get Array of doctor ID with PA appointments
			String query = "SELECT D.doctor_ID, COUNT(A.appnt_ID) FROM Doctor D, Appointment A, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND A.status = \'PA\' GROUP BY D.doctor_ID ORDER BY D.doctor_ID ASC;";
			rs3 = esql.executeQueryAndReturnResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error!!");
		}
		for (int i = 0; i < rs3.size(); i++) { // Insert found data into the Container
			Container.get(Integer.parseInt(rs3.get(i).get(0))).set(1, rs3.get(i).get(1));
		}

		try { // Get Array of doctor ID with AC appointments
			String query = "SELECT D.doctor_ID, COUNT(A.appnt_ID) FROM Doctor D, Appointment A, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND A.status = \'AC\' GROUP BY D.doctor_ID ORDER BY D.doctor_ID ASC;";
			rs3 = esql.executeQueryAndReturnResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error!!");
		}
		for (int i = 0; i < rs3.size(); i++) { // Insert found data into the Container
			Container.get(Integer.parseInt(rs3.get(i).get(0))).set(2, rs3.get(i).get(1));
		}

		try { // Get Array of doctor ID with AV appointments
			String query = "SELECT D.doctor_ID, COUNT(A.appnt_ID) FROM Doctor D, Appointment A, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND A.status = \'AV\' GROUP BY D.doctor_ID ORDER BY D.doctor_ID ASC;";
			rs3 = esql.executeQueryAndReturnResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error!!");
		}
		for (int i = 0; i < rs3.size(); i++) { // Insert found data into the Container
			Container.get(Integer.parseInt(rs3.get(i).get(0))).set(3, rs3.get(i).get(1));
		}

		try { // Get Array of doctor ID with WL appointments
			String query = "SELECT D.doctor_ID, COUNT(A.appnt_ID) FROM Doctor D, Appointment A, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND A.status = \'WL\' GROUP BY D.doctor_ID ORDER BY D.doctor_ID ASC;";
			rs3 = esql.executeQueryAndReturnResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error!!");
		}
		for (int i = 0; i < rs3.size(); i++) { // Insert found data into the Container
			Container.get(Integer.parseInt(rs3.get(i).get(0))).set(4, rs3.get(i).get(1));
		}

		List<List<String>> Status_Holder = new ArrayList<List<String>>(); // Initialize container for status values
		for (int i = 0; i <= arraySize; i++) {
			List<String> index = new ArrayList<String>();
			index.add("PA");
			index.add("AC");
			index.add("AV");
			index.add("WL");
			Status_Holder.add(index);
		}

		for (int i = 0; i <= arraySize; i++) { // Begin sorting per row
			int maxV = Integer.parseInt(Container.get(i).get(1));
			int index = 1;
			String temp = Container.get(i).get(1);
			String temp2 = Status_Holder.get(i).get(0);
			for (int j = 2; j <= 4; i++) {
				if (Integer.parseInt(Container.get(i).get(j)) > maxV) {
					maxV = Integer.parseInt(Container.get(i).get(j));
					index = j;
				}
			} // Swapping first and max
			Container.get(i).set(1, Container.get(i).get(index));
			Container.get(i).set(index, temp);
			Status_Holder.get(i).set(0, Status_Holder.get(i).get(index - 1));
			Status_Holder.get(i).set(index - 1, temp2);
		}

		for (int i = 0; i <= arraySize; i++) { // Print all values in our format
			System.out.println("DoctorID " + i + " " + Container.get(i).get(1) + Status_Holder.get(i).get(0) + Container.get(i).get(2) + Status_Holder.get(i).get(1) + Container.get(i).get(3) + Status_Holder.get(i).get(2) + Container.get(i).get(4) + Status_Holder.get(i).get(3));
		}
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		String status;

		do { // status
			System.out.print("Input Appointment's Status (PA, AC, AV, WL):");
			try {
				status = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		try { // Run the query
			String query = "SELECT D.doctor_ID, D.name, COUNT(A.appnt_ID) AS TotalPatients FROM Doctor D, Appointment A, has_appointment H WHERE D.doctor_ID = H.doctor_ID AND H.appt_ID = A.appnt_ID AND A.status = \'" + status + "\' GROUP BY D.doctor_ID ORDER BY D.doctor_ID ASC;";
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.out.println("Table Search Error! Please double check values!");
		}
	}
}