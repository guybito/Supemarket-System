package DataAccessLayer.InventoryDataAccessLayer;

import BusinessLayer.InventoryBusinessLayer.*;

import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public interface ReportDao {
    public Map<Integer, MissingProductsReport> getAllMissingReports() throws SQLException;
    public Map<Integer, DefectiveProductsReport> getAllDefectiveReports() throws SQLException;
    public Map<Integer, WeeklyStorageReport> getAllWeeklyReports() throws SQLException;
    public Report getReportByID(int reportID) throws SQLException;
    public void addReport(Report report) throws SQLException;
    void addLineToWeeklyReport(int reportID, int categoryID, int productID, int amountInBranch) throws SQLException;
    void addLineToMissingReport(int reportID, int productID, int amountInBranch) throws SQLException;
    void addLineToDefectiveReport(int reportID, int itemID) throws SQLException;
    public int getNewReportID() throws SQLException;
}
