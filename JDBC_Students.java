import java.io.File;
import java.io.PrintWriter;
import java.sql.*;

public class JDBC_Students {
	public static void main(String[] args) throws Exception {
		// Load and register a JDBC driver
				try {
					// Load the driver (registers itself)
					Class.forName("com.mysql.cj.jdbc.Driver");
				} catch (Exception E) {
					System.err.println("Unable to load driver.");
					E.printStackTrace();
				}
				try {
					//output file for Part B
					File file = new File("JDBC_StudentsOutput.txt");
					PrintWriter pw = new PrintWriter(file);
					
					// Connect to the database
					Connection conn1;
					String dbUrl = "jdbc:mysql://csdb.cs.iastate.edu:3306/db363samiw";
					String user = "dbu363samiw";
					String password = "VdgJ4317";
					conn1 = DriverManager.getConnection(dbUrl, user, password);
					System.out.println("*** Connected to the database ***");
					System.out.println("Processing...");

					//Part A
					updateStudents(conn1);
					
					//Part B
					topSeniors(conn1, pw);
					
					conn1.close();
					System.out.println("*** Connection Closed ***");
					
				} catch (SQLException e) {
					System.out.println("SQLException: " + e.getMessage());
					System.out.println("SQLState: " + e.getSQLState());
					System.out.println("VendorError: " + e.getErrorCode());
				}
	
	}
	
	//Function for Part A
	public static void updateStudents(Connection conn1) throws SQLException {
		// Create Statement and ResultSet variables to use throughout the project
		Statement s = conn1.createStatement();
		Statement s1 = conn1.createStatement();
		ResultSet rs;

		// get salaries of all instructors
		rs = s.executeQuery("select s.StudentID, s.GPA, s.Classification, s.CreditHours " + 
									"from Student s");
		//update statement
		String updateStatement = "update Student set GPA = ?, Classification = ?, CreditHours = ? where StudentID = ?";
		PreparedStatement ps = conn1.prepareStatement(updateStatement);

		//go through each student
		while (rs.next()) {
			int id = rs.getInt("StudentID");
			float gpa = rs.getFloat("GPA");
			String classification = rs.getString("Classification");
			int credits = rs.getInt("CreditHours");
			float totalPoints = gpa*credits;


			ResultSet rs1;
			rs1 = s1.executeQuery("select e.Grade " +
											"from Enrollment e " + 
											"where e.StudentID=" + id);
			//go through each class affiliated with this student
			while (rs1.next()) {
				float grade = qtyPoints(rs1.getString("Grade").trim());
				totalPoints = totalPoints + (grade*3);
				credits = credits + 3;
			}
			gpa = totalPoints/credits;
			classification = newClassification(credits);
			
			//update statements
			ps.setFloat(1, gpa);
			ps.setString(2,  classification);
			ps.setInt(3,  credits);
			ps.setInt(4, id);
			
			ps.executeUpdate();
			

			
		}

		// Close all statements and connections
		s.close();
		s1.close();
		rs.close();
		ps.close();


	}
	
	//Function for Part B
	public static void topSeniors(Connection conn1, PrintWriter pw) throws SQLException {
		//Headers for output file
		pw.println("Top Seniors");
		pw.println();
		String name = "Student Name:";
		String mname = "Mentor Name:";
		String gp = "GPA:";
		pw.printf("%-30s %-30s %-5s", name, mname, gp);
		pw.println();
		//Query
		Statement s = conn1.createStatement();
		ResultSet rs = s.executeQuery("select p.Name, p1.Name, s.GPA "
				+ "from Person p, Person p1, Student s "
				+ "where p.ID in (select s.StudentID from Student s where s.Classification='Senior') "
				+ "and p1.ID in  (select s.MentorID from Student s where s.StudentID=p.ID) "
				+ "and s.StudentID = p.ID " 
				+ "order by s.GPA DESC");
		int count = 0;
		float gpa = 0;
		String studentName;
		String mentorName;
		int num =0;
		//Get top 5 GPAs
		while (rs.next() && count<5) {
			num++;
			studentName = rs.getString(1);
			mentorName = rs.getString(2);
			gpa = rs.getFloat(3);
			double x = Math.round(gpa*100)/100.0;
			pw.printf("%-30s %-30s %-5s", studentName, mentorName, x);
			pw.println();

			count++;
		}
		//Check if anyone tied for 5th highest
		while (rs.next() && rs.getFloat(3)==gpa) {
			num++;
			studentName = rs.getString(1);
			mentorName = rs.getString(2);
			double x = Math.round(gpa*100)/100.0;
			pw.printf("%-30s %-30s %-5s", studentName, mentorName, x);
			pw.println();

			
		}
		// Close all statements and connections
		s.close();
		rs.close();
		pw.close();

		
	}
	
	//Get points from letter grade
	public static float qtyPoints(String letter) {
		if (letter.equals("A")) {
			return 4;
		} else if (letter.equals("A-")) {
			return (float) 3.66;
		} else if (letter.equals("B+")) {
			return (float) 3.33;
		} else if (letter.equals("B")) {
			return 3;
		} else if (letter.equals("B-")) {
			return (float) 2.66;
		} else if (letter.equals("C+")) {
			return (float) 2.33;
		} else if (letter.equals("C")) {
			return 2;
		} else if (letter.equals("C-")) {
			return (float) 1.66;
		} else if (letter.equals("D+")) {
			return (float) 1.33;
		} else if (letter.equals("D")) {
			return 1;
		} else {
			return 0;
		}
	}
	
	//Return Classification based off total credit hours
	public static String newClassification(int credits) {
		if (0<=credits && credits<=29) {
			return "Freshman";
		} else if (30<=credits && credits<=59) {
			return "Sophomore";
		} else if (60<=credits && credits<=89) {
			return "Junior";
		} else {
			return "Senior";
		}
	}
	
}
