import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static final String sql_procedure = """
            CREATE PROCEDURE `negoci`.`get_department` (IN `id` INT, OUT `num_employees` INT, OUT `avg_salary` INT)
            main: BEGIN
                IF NOT EXISTS (SELECT * FROM DEPT WHERE DEPT_NO = id) THEN
                    SET num_employees = 0;
                    SET avg_salary = -1;
                    LEAVE main;
                END IF;
            
                SET num_employees = (SELECT (COUNT(*)) FROM DEPT WHERE DEPT_NO = id);
            
                IF num_employees = 0 THEN
                    SET avg_salary = 0;
                    LEAVE main;
                END IF;
            
                SET avg_salary = (SELECT AVG(SALARI)  FROM EMP WHERE DEPT_NO = id);
            
                IF avg_salary IS NULL THEN
                    SET avg_salary = 0;
                END IF;
            END;
            """;

    /// que mostri tots els departaments amb les seves dades, incloent el nombre d’empleats i el salari mitjà.
    public static void main(String[] args) throws SQLException {
        try (var a = getConnection()) {
            createProcedure(a);

            var stmt = a.createStatement();
            var rs = stmt.executeQuery("SELECT * FROM DEPT");

            while (rs.next()) {
                var id = rs.getInt("DEPT_NO");
                var name = rs.getString("DNOM");
                var location = rs.getString("LOC");

                var prepareCall = a.prepareCall("CALL get_department(?,?,?)");
                prepareCall.setInt(1, id);
                prepareCall.registerOutParameter(2, java.sql.Types.INTEGER);
                prepareCall.registerOutParameter(3, java.sql.Types.INTEGER);
                prepareCall.execute();

                var num_employees = prepareCall.getInt(2);
                var avg_salary = prepareCall.getInt(3);

                System.out.println("id: " + id + ", name: " + name + ", location: " + location + ", num_employees: " + num_employees + ", avg_salary: " + avg_salary);

            }

        }
    }

    private static void createProcedure(Connection a) throws SQLException {
        var stmt = a.createStatement();
        stmt.execute("DROP PROCEDURE IF EXISTS get_department");
        stmt.execute(sql_procedure);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/negoci", "root", "CalaClara21.");
    }
}

