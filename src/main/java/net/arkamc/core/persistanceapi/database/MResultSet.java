package net.arkamc.core.persistanceapi.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

@SuppressWarnings("unused")
public class MResultSet {
    private final HashMap<Integer, ResultSetRow> elements = new HashMap<>();
    private int index = -1;
    private int size;
    private MResultSetMetaData metadata;

    public MResultSet(ResultSet req) {
        try {
            ResultSetMetaData meta = req.getMetaData();
            int columnCount = meta.getColumnCount();
            int index_rows = 0;
            HashMap<Integer, String> columnsname = new HashMap<>();

            while(req.next()) {
                HashMap<String, Object> columns = new HashMap<>();

                for(int column = 1; column <= columnCount; ++column) {
                    if (index_rows == 0) {
                        columnsname.put(column, meta.getColumnName(column));
                    }

                    columns.put(meta.getColumnName(column), req.getObject(column));
                }

                ResultSetRow row = new ResultSetRow(columns);
                this.elements.put(index_rows++, row);
            }

            if (index_rows == 0) {
                this.index = -42;
            }

            this.size = index_rows;
            this.metadata = new MResultSetMetaData(columnCount, columnsname);
        } catch (SQLException var8) {
            var8.printStackTrace();
        }

    }

    public boolean first() {
        if (this.index == -42) {
            return false;
        } else {
            this.index = 0;
            return true;
        }
    }

    public boolean last() {
        if (this.index == -42) {
            return false;
        } else {
            this.index = this.size - 1;
            return true;
        }
    }

    public int getRow() {
        return this.index + 1;
    }

    public boolean beforeFirst() {
        if (this.index == -42) {
            return false;
        } else {
            this.index = -1;
            return true;
        }
    }

    public boolean next() {
        ++this.index;
        return this.elements.containsKey(this.index);
    }

    public String getString(String columnName) {
        if (!this.elements.containsKey(this.index)) {
            return null;
        } else {
            return this.elements.get(this.index).elements.containsKey(columnName) && this.elements.get(this.index).elements.get(columnName) != null ? this.elements.get(this.index).elements.get(columnName).toString() : null;
        }
    }

    public int getInt(String columnName) {
        if (!this.elements.containsKey(this.index)) {
            return -1;
        } else {
            return this.elements.get(this.index).elements.containsKey(columnName) && this.elements.get(this.index).elements.get(columnName) != null ? Integer.parseInt(this.elements.get(this.index).elements.get(columnName).toString()) : -1;
        }
    }

    public long getLong(String columnName) {
        if (!this.elements.containsKey(this.index)) {
            return -1L;
        } else {
            return this.elements.get(this.index).elements.containsKey(columnName) && this.elements.get(this.index).elements.get(columnName) != null ? Long.parseLong(this.elements.get(this.index).elements.get(columnName).toString()) : -1L;
        }
    }

    public double getDouble(String columnName) {
        if (!this.elements.containsKey(this.index)) {
            return -1.0;
        } else {
            return this.elements.get(this.index).elements.containsKey(columnName) && this.elements.get(this.index).elements.get(columnName) != null ? Double.parseDouble(this.elements.get(this.index).elements.get(columnName).toString()) : -1.0;
        }
    }

    public byte getByte(String columnName) {
        if (!this.elements.containsKey(this.index)) {
            return -1;
        } else {
            return !this.elements.get(this.index).elements.containsKey(columnName) ? -1 : Byte.parseByte(this.elements.get(this.index).elements.get(columnName).toString());
        }
    }

    public byte[] getBytes(String columnName) {
        if (!this.elements.containsKey(this.index)) {
            return new byte[0];
        } else {
            return !this.elements.get(this.index).elements.containsKey(columnName) ? new byte[0] : (byte[]) this.elements.get(this.index).elements.get(columnName);
        }
    }

    public MResultSetMetaData getMetaData() {
        return this.metadata;
    }

    private static class ResultSetRow {
        private final HashMap<String, Object> elements;

        public ResultSetRow(HashMap<String, Object> elements) {
            this.elements = elements;
        }
    }

    public static class MResultSetMetaData {
        private final int columnslength;
        private final HashMap<Integer, String> columnnames;

        public MResultSetMetaData(int columnslength, HashMap<Integer, String> columnsnames) {
            this.columnslength = columnslength;
            this.columnnames = columnsnames;
        }

        public int getColumnCount() {
            return this.columnslength;
        }

        public String getColumnName(int columnindex) {
            return this.columnnames.get(columnindex);
        }
    }
}