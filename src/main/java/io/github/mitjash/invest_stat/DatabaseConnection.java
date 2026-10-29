package io.github.mitjash.invest_stat;

import ru.tinkoff.piapi.contract.v1.OperationItem;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DatabaseConnection {

    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private Connection conn;

    // Конструктор
    private DatabaseConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "root", "123456");
        } catch (SQLException e) {
            System.out.println("DB Connection Error: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    // --- ОБЫЧНЫЕ МЕТОДЫ КЛАССА ---

    public void insertMyPositions(ArrayList<MyPosition> positions)  {
        String uploadId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Insert positions
        int affectedRows = 0;
        for (var position : positions) {

            try {
                String sql =
                        "INSERT INTO invest_stat.stg_positions " +
                        "(upload_id, account_id, instrument_uid, instrument_type, name, brand, quantity, current_price, ccy, figi, ticker, isin, asset_uid, blocked_quantity, blocked, trading_status, average_price, " +
                        "bond_nominal, bond_type, bond_sector, bond_maturity_date, bond_callput_date, insert_time) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                        "?, ?, ?, ?, ?, current_timestamp)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uploadId);
                pstmt.setString(2, position.getAccountId());
                pstmt.setString(3, position.getInstrumentUid());
                pstmt.setString(4, position.getInstrumentType());
                pstmt.setString(5, position.getName());
                pstmt.setString(6, position.getBrand());
                pstmt.setInt(7, position.getQuantity());
                pstmt.setBigDecimal(8, position.getCurrentPrice());
                pstmt.setString(9, position.getCcy());
                pstmt.setString(10, position.getFigi());
                pstmt.setString(11, position.getTicker());
                pstmt.setString(12, position.getIsin());
                pstmt.setString(13, position.getAssetUid());
                pstmt.setInt(14, position.getBlockedQuantity());
                pstmt.setBoolean(15, position.getBlocked());
                pstmt.setInt(16, position.getTradingStatus());
                pstmt.setBigDecimal(17, position.getAveragePrice());

                pstmt.setBigDecimal(18, position.getNominal());
                pstmt.setInt(19, position.getBondType());
                pstmt.setString(20, position.getBondSector());
                pstmt.setDate(21, position.getBondMaturityDate());
                pstmt.setDate(22, position.getBondCallPutDate());

                affectedRows += pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Insert Error: " + e.getMessage());
                return;
            }
        }
        System.out.println("Inserted Position rows: " + affectedRows);

        // Insert upload record
        try {
            String sql = "INSERT INTO invest_stat.stg_upload (upload_id, upload_type, row_qty, status) VALUES (?, 'Positions', ?, 'U')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uploadId);
            pstmt.setInt(2, affectedRows);
            affectedRows = pstmt.executeUpdate();
            System.out.println("Inserted Upload rows: " + affectedRows);
        } catch (SQLException e) {
            System.out.println("Insert Error: " + e.getMessage());
        }
    }

    public void insertOperations(ArrayList<OperationItem> operations) {
        String uploadId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // pstmt.setDouble(2, 123.456);

        // Insert positions
        int affectedRows = 0;
        for (OperationItem operation : operations) {

            try {
                String sql =
                        "INSERT INTO invest_stat.operations " +
                                "(upload_id, id, account_id, instrument_uid, operation_time, operation_type" +
                                ", amount, price, ccy, quantity, commission, accrued_int, status, ticker, figi, insert_time" +
                                //", instrument_type, status_name, operation_type_name" +
                                //") VALUES (?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, current_timestamp, ?, ?, ?)";
                                ") VALUES (?, ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uploadId);
                pstmt.setString(2, operation.getId());
                pstmt.setString(3, operation.getBrokerAccountId());
                pstmt.setString(4, operation.getInstrumentUid());
                pstmt.setTimestamp(5, java.sql.Timestamp.from(Instant.ofEpochSecond(operation.getDate().getSeconds())));
                pstmt.setInt(6, operation.getType().getNumber());

                //operation.gett

                pstmt.setBigDecimal(7, TBankConnection.toBigDecimal(operation.getPayment()));
                pstmt.setBigDecimal(8, TBankConnection.toBigDecimal(operation.getPrice()));
                pstmt.setString(9, operation.getPayment().getCurrency());
                pstmt.setInt(10, (int)operation.getQuantity());
                pstmt.setBigDecimal(11, TBankConnection.toBigDecimal(operation.getCommission()));
                pstmt.setBigDecimal(12, TBankConnection.toBigDecimal(operation.getAccruedInt()));
                pstmt.setInt(13, operation.getState().getNumber());
                pstmt.setString(14, operation.getTicker());
                pstmt.setString(15, operation.getFigi());


                //pstmt.setString(14, operation.getInstrumentType());
                //pstmt.setString(15, operation.getState().name());
                //pstmt.setString(16, operation.getType().name());




                /*pstmt.setString(2, position.getAccountId());
                pstmt.setString(3, position.getInstrumentUid());
                pstmt.setString(4, position.getInstrumentType());
                pstmt.setString(5, position.getName());
                pstmt.setString(6, position.getBrand());
                pstmt.setInt(7, position.getQuantity());
                pstmt.setBigDecimal(8, position.getCurrentPrice());
                pstmt.setString(9, position.getCcy());
                pstmt.setString(10, position.getFigi());
                pstmt.setString(11, position.getTicker());
                pstmt.setString(12, position.getIsin());
                pstmt.setString(13, position.getAssetUid());*/
                affectedRows += pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Insert Error: " + e.getMessage());
                return;
            }
        }
        System.out.println("Inserted Operation rows: " + affectedRows);

        // Insert upload record
        if (affectedRows>0) {
            try {
                String sql = "INSERT INTO invest_stat.stg_upload (upload_id, upload_type, row_qty, status) VALUES (?, 'Operations', ?, 'U')";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uploadId);
                pstmt.setInt(2, affectedRows);
                affectedRows = pstmt.executeUpdate();
                System.out.println("Inserted Upload rows: " + affectedRows);
            } catch (SQLException e) {
                System.out.println("Insert Error: " + e.getMessage());
            }
        }
    }

    public Instant getLastOrationTime()  {
        Instant res = null;
        try {
            String sql = "select max(operation_time) mot from invest_stat.operations";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                LocalDateTime ldt = rs.getObject("mot", LocalDateTime.class);
                if (ldt  != null) {
                    res = ldt.toInstant(ZoneOffset.ofHours(3));
                }
            }
        } catch (SQLException e) {
            System.out.println("Select Error: " + e.getMessage());
        }

        return res;
    }

    public ArrayList<String> getUnkownInstuments()  {
        ArrayList<String> res = new ArrayList<String>();
        try {
            String sql =
                    "select distinct o.instrument_uid \n" +
                    "  from invest_stat.operations o\n" +
                    "  left join invest_stat.instruments i on o.instrument_uid = i.instrument_uid\n" +
                    " where o.instrument_uid is not null\n" +
                    "        and o.instrument_uid <> ''" +
                    "   and i.instrument_uid is null";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String col_value = rs.getString("instrument_uid");
                res.add(col_value);
            }
        } catch (SQLException e) {
            System.out.println("Select Error: " + e.getMessage());
        }

        return res;
    }

    public void insertOldInstuments(ArrayList<String> instrument_ids)  {
        String uploadId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String dbgSQL = "";

        // Insert positions
        int affectedRows = 0;
        for (String instrument_id : instrument_ids) {
            MyPosition position = new MyPosition(instrument_id);
            if (position.getInstrumentType() == null) {
                continue;
            }
            try {
                String sql =
                        "INSERT INTO invest_stat.stg_positions " +
                                "(upload_id, account_id, instrument_uid, instrument_type, name, brand, quantity, current_price, ccy, figi, ticker, isin, asset_uid, blocked_quantity, blocked, trading_status, average_price, " +
                                "bond_nominal, bond_type, bond_sector, bond_maturity_date, bond_callput_date, insert_time) VALUES " +
                                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                                "?, ?, ?, ?, ?, current_timestamp)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, uploadId);
                pstmt.setString(2, position.getAccountId());
                pstmt.setString(3, position.getInstrumentUid());
                pstmt.setString(4, position.getInstrumentType());
                pstmt.setString(5, position.getName());
                pstmt.setString(6, position.getBrand());
                pstmt.setInt(7, position.getQuantity());
                pstmt.setBigDecimal(8, position.getCurrentPrice());
                pstmt.setString(9, position.getCcy());
                pstmt.setString(10, position.getFigi());
                pstmt.setString(11, position.getTicker());
                pstmt.setString(12, position.getIsin());
                pstmt.setString(13, position.getAssetUid());
                pstmt.setInt(14, position.getBlockedQuantity());
                pstmt.setBoolean(15, position.getBlocked());
                pstmt.setInt(16, position.getTradingStatus());
                pstmt.setBigDecimal(17, position.getAveragePrice());

                pstmt.setBigDecimal(18, position.getNominal());
                pstmt.setInt(19, position.getBondType());
                pstmt.setString(20, position.getBondSector());
                pstmt.setDate(21, position.getBondMaturityDate());
                pstmt.setDate(22, position.getBondCallPutDate());

                dbgSQL = pstmt.toString();
                affectedRows += pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Insert Error: " + e.getMessage());
                System.out.println(dbgSQL);
                return;
            }
        }
        System.out.println("Inserted Position rows: " + affectedRows);

        // Insert upload record
        try {
            String sql = "INSERT INTO invest_stat.stg_upload (upload_id, upload_type, row_qty, status) VALUES (?, 'Positions', ?, 'U')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uploadId);
            pstmt.setInt(2, affectedRows);
            affectedRows = pstmt.executeUpdate();
            System.out.println("Inserted Upload rows: " + affectedRows);
        } catch (SQLException e) {
            System.out.println("Insert Error: " + e.getMessage());
        }
    }
}
