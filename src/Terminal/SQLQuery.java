package Terminal;

import java.util.List;

public class SQLQuery {
    String Name;
    String SQL;

    public SQLQuery(String name, String SQL) {
        Name = name;
        this.SQL = SQL;
    }

    @Override
    public String toString() {
        return Name;
    }
}
