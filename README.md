# GY10 - Adatbázis

> `-cp`: class path
> 
> Parancssorból futtatás: `-cp .:sqlite-jdbc-3.21.0.jar DbExampleSql`, a függőségekről bővebben a `pom.xml` fájl mesél.

## Csatlakozás szerverhez

`Connection conn = DriverManager.getConnection(url, user, password);`  azaz a `jdbc` segítésgével péládul így érjük el a fájlt `sqlite` segítségével: `String url = "jdbc:sqlite:sqlite_test.db";` ellenben a `hsqldb`  `String url = "jdbc:hsqldb:file:hsqldb_dir/hsql_test_db";` fájlokkal is tud dolgozni és adatbázis szervert is. Az `sqlite` csak egyetlen fájlt készít a `hsqldb` pár darab fájlt készít.

pl:
````Java
String user = "sa";
        String password = "";
        String url = "jdbc:hsqldb:file:hsqldb_dir/hsql_test_db";

        loadDbDriver("org.hsqldb.jdbc.JDBCDriver");
````

### Kapcsolódás

````Java
Connection conn = DriverManager.getConnection(url, user, password);
````

## Műveletek
### Tábla létrehozása
````Java
private static void createTables(Connection conn) throws SQLException {
        try (
            Statement stat = conn.createStatement();
        ) {
            stat.executeUpdate("drop table if exists people;");
            stat.executeUpdate("create table people (name varchar(80), birthyear int);");
        }
    }
````
Azaz dobjuk ki a táblát de csak ha létezik: `stat.executeUpdate("drop table if exists people;");` vagy csak hozzuk létre: `stat.executeUpdate("create table people (name varchar(80), birthyear int);");`

### Feltöltés tartalommal
Itt `PreparedStatement` segítésgével töltjük fel.
````Java
private static void insertData(Connection conn) throws SQLException {
        try (
            PreparedStatement prep = conn.prepareStatement("insert into people values (?, ?);");
        ) {
            addPerson(prep, "Gandhi", 1869);
            addPerson(prep, "Turing", 1912);
            addPerson(prep, "Wittgenstein", 1889);
            addPerson(prep, "Frege", 1848);

            conn.setAutoCommit(false);
            prep.executeBatch();
            conn.setAutoCommit(true);
        }
    }
````

Itt a `prepareStatement("insert into people values (?, ?);");` utasításon belül a `?`re illeszkedik az oda való adattag, pl:
`"Wittgenstein"` és `1889`.

A `conn.setAutoCommit(false); prep.executeBatch();` részben meg azt állítjuk be, hogy egyben történjen az egész.

### Lekérdezés
Lekérdezni `executeQuery`vel tudunk.
````Java
private static void queryData2(Connection conn) throws SQLException {
        try (
            PreparedStatement p = conn.prepareStatement("select * from people p where p.birthyear < ? and p.name <> ?;");
            PreparedStatement p2 = conn.prepareStatement("select count(*) from people p where p.birthyear < ? and p.name <> ?;");
        ) {
            p.setInt(1, 1900);
            p.setString(2, "Wittgenstein");
            p2.setInt(1, 1900);
            p2.setString(2, "Wittgenstein");

            try (
                ResultSet rs = p.executeQuery();
                ResultSet rs2 = p2.executeQuery();
            ) {
                rs2.next();
                int resultCount = rs2.getInt(1);
                System.out.println("count = " + resultCount);

                while (rs.next()) {
                  String name = rs.getString("name");
                  int birthyear = rs.getInt("birthyear");
                  System.out.printf("name = %s, birthyear = %d%n", name, birthyear);
                }
            }
        }
    }
````
De előbb beállítjuk az **SQL utasítást**: `prepareStatement("select * from people p where p.birthyear < ? and p.name <> ?;");` Ezen belül a paraméterek szintén `?`lel vannak megadva. Itt szintén helyettesítődik a `p.``setInt`/`setString` stb alakban. Természetesen **beégetve is működik**. És **működik `String` kezeléses** összefűzéssel is: `+`, **de így SQL Injection ellen nincs védelem**.

`ResultSet `tel tudjuk bejárni az adattáblánkat.
