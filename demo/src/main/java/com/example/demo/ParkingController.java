package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class ParkingController {

    Connection con;

    public ParkingController() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/parking_system",
                "root",
                "gowtham@2203"   // 🔴 CHANGE THIS to your MySQL password
        );
    }

    // 🚗 PARK VEHICLE
    @PostMapping("/park")
    public String park(@RequestParam String vehicle, @RequestParam String owner) throws Exception {

        String insertVehicle = "INSERT INTO Vehicles(vehicle_number, owner_name) VALUES(?,?)";
        PreparedStatement ps1 = con.prepareStatement(insertVehicle, Statement.RETURN_GENERATED_KEYS);
        ps1.setString(1, vehicle);
        ps1.setString(2, owner);
        ps1.executeUpdate();

        ResultSet rs = ps1.getGeneratedKeys();
        rs.next();
        int vehicleId = rs.getInt(1);

        String getSlot = "SELECT * FROM Slots WHERE status='FREE' LIMIT 1";
        Statement st = con.createStatement();
        ResultSet rs2 = st.executeQuery(getSlot);

        if (!rs2.next()) return "No slots available";

        int slotId = rs2.getInt("id");
        String slotNumber = rs2.getString("slot_number");

        String insertRecord = "INSERT INTO ParkingRecords(vehicle_id, slot_id, entry_time) VALUES(?,?,NOW())";
        PreparedStatement ps2 = con.prepareStatement(insertRecord);
        ps2.setInt(1, vehicleId);
        ps2.setInt(2, slotId);
        ps2.executeUpdate();

        PreparedStatement ps3 = con.prepareStatement("UPDATE Slots SET status='OCCUPIED' WHERE id=?");
        ps3.setInt(1, slotId);
        ps3.executeUpdate();

        return "Vehicle parked in slot " + slotNumber;
    }

    // 🚪 EXIT VEHICLE
    @PostMapping("/exit")
    public String exit(@RequestParam String vehicle) throws Exception {

        String find = "SELECT pr.id, pr.entry_time, pr.slot_id FROM ParkingRecords pr " +
                "JOIN Vehicles v ON pr.vehicle_id = v.id " +
                "WHERE v.vehicle_number=? AND pr.exit_time IS NULL";

        PreparedStatement ps = con.prepareStatement(find);
        ps.setString(1, vehicle);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) return "Vehicle not found or already exited";

        int recordId = rs.getInt("id");
        int slotId   = rs.getInt("slot_id");

        Timestamp entryTime = rs.getTimestamp("entry_time");
        long hours = (System.currentTimeMillis() - entryTime.getTime()) / (1000 * 60 * 60);
        if (hours == 0) hours = 1;
        double fee = hours * 20;

        String update = "UPDATE ParkingRecords SET exit_time=NOW(), fee=? WHERE id=?";
        PreparedStatement ps2 = con.prepareStatement(update);
        ps2.setDouble(1, fee);
        ps2.setInt(2, recordId);
        ps2.executeUpdate();

        PreparedStatement ps3 = con.prepareStatement("UPDATE Slots SET status='FREE' WHERE id=?");
        ps3.setInt(1, slotId);
        ps3.executeUpdate();

        return "Vehicle exited. Fee: ₹" + (int) fee;
    }

    // 📊 VIEW SLOTS
    @GetMapping("/slots")
    public List<Map<String, Object>> slots() throws Exception {

        List<Map<String, Object>> list = new ArrayList<>();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM Slots ORDER BY id");

        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",          rs.getInt("id"));
            map.put("slot_number", rs.getString("slot_number"));
            map.put("status",      rs.getString("status"));
            list.add(map);
        }

        return list;
    }

    // ✅ NEW: VIEW ALL PARKING RECORDS  ← THIS IS WHAT WAS MISSING
    @GetMapping("/records")
    public List<Map<String, Object>> records() throws Exception {

        List<Map<String, Object>> list = new ArrayList<>();

        String query = "SELECT pr.id, v.vehicle_number, v.owner_name, " +
                       "pr.entry_time, pr.exit_time, pr.fee " +
                       "FROM ParkingRecords pr " +
                       "JOIN Vehicles v ON pr.vehicle_id = v.id " +
                       "ORDER BY pr.id DESC";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",             rs.getInt("id"));
            map.put("vehicle_number", rs.getString("vehicle_number"));
            map.put("owner_name",     rs.getString("owner_name"));
            map.put("entry_time",     rs.getTimestamp("entry_time"));
            map.put("exit_time",      rs.getTimestamp("exit_time"));   // null if still parked
            map.put("fee",            rs.getObject("fee"));             // null if still parked
            list.add(map);
        }

        return list;
    }
}
