import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/integrals";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1"; // измените под свой пароль

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS integrals (" +
                "id SERIAL PRIMARY KEY," +
                "lower_limit NUMERIC NOT NULL," +
                "upper_limit NUMERIC NOT NULL," +
                "step NUMERIC NOT NULL," +
                "result NUMERIC)";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void dropTable() {
        String sql = "DROP TABLE IF EXISTS integrals";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addRecord(RecIntegral recIntegral) {
        String sql = "INSERT INTO integrals (lower_limit, upper_limit, step, result) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, recIntegral.getLowerLimit());
            pstmt.setDouble(2, recIntegral.getUpperLimit());
            pstmt.setDouble(3, recIntegral.getStep());
            pstmt.setDouble(4, recIntegral.getResult());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Добавлено строк: " + rowsAffected);
    } catch (SQLException e) {
        e.printStackTrace();}
    }

    public static RecIntegral getLastRecords(){
        String sql = "SELECT * FROM integrals ORDER BY id DESC LIMIT 1;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                double lower = rs.getDouble("lower_limit");
                double upper = rs.getDouble("upper_limit");
                double step = rs.getDouble("step");
                double result = rs.getDouble("result");
                return new RecIntegral(lower, upper, step, result);
            } else {
                return null; // таблица пуста
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<RecIntegral> getRecords(){
        String sql = "SELECT * FROM integrals";
        List<RecIntegral> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                double lower = rs.getDouble("lower_limit");
                double upper = rs.getDouble("upper_limit");
                double step = rs.getDouble("step");
                double result = rs.getDouble("result");
                list.add(new RecIntegral(lower, upper, step, result));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updateRecord(RecIntegral recIntegral) {
        String sql = "UPDATE integrals SET lower_limit = ?, upper_limit = ?, step = ?, result = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, recIntegral.getLowerLimit());
            pstmt.setDouble(2, recIntegral.getUpperLimit());
            pstmt.setDouble(3, recIntegral.getStep());
            pstmt.setDouble(4, recIntegral.getResult());
            pstmt.setInt(5, recIntegral.getId()); // используем id из объекта
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Обновлено строк: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeRecord(RecIntegral recIntegral) {
        String sql = "DELETE FROM integrals WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recIntegral.getId());
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Удалено строк: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}