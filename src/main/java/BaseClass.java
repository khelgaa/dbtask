
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;


public class BaseClass {

    private static final String FIRST_FILE = "1.xml";
    private static final String SECOND_FILE = "2.xml";
    private static final String TRANSFORM_FILE = "src/main/resources/transform.xsl";

    private Integer N;

    private Connection connection;

    public Integer getN() {
        return N;
    }

    public void setN(Integer n) {
        this.N = n;
    }

    public Connection getConnection() {
        return connection;
    }

    public BaseClass() throws IOException, SQLException {

        String url = "jdbc:mysql://localhost:3306/testdb";
        String username = "user";
        String password = "1234";
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            createTable();
        } finally {
            if(statement != null)
                statement.close();
        }
    }


    private void createTable() throws SQLException {
        try(Statement statement = connection.createStatement()) {
            DatabaseMetaData databaseMetadata = connection.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null, "test", null);
            if (resultSet.next()) {
                statement.executeUpdate("DELETE FROM TEST");
            } else{
                statement.executeUpdate("CREATE TABLE TEST (field INTEGER)");
            }
        }
    }




    public void insertData() {
        try(PreparedStatement ps = connection.prepareStatement("INSERT INTO TEST (field) values(?)")) {
            for (int i = 1; i <= N; i++) {
                ps.setInt(1, i);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private  void writeToFile(String str, String fileName){
        Path path = Paths.get(fileName);
        try(BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String createFirstFile() throws ParserConfigurationException, SQLException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("entries");

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT field FROM TEST");
        while (resultSet.next()) {
            Element entry = doc.createElement("entry");
            Element field = doc.createElement("field");
            field.setTextContent(resultSet.getString(1));
            entry.appendChild(field);
            root.appendChild(entry);
        }

        resultSet.close();
        statement.close();

        doc.appendChild(root);
        String res = documentToString(doc);
        writeToFile(res, FIRST_FILE);
        return res;
    }



    private String documentToString(Document doc) throws TransformerException {

        TransformerFactory tFactory= TransformerFactory.newInstance();
        Transformer transformer;
        transformer = tFactory.newTransformer();

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();
        }





   public void createSecondFile() throws TransformerException, IOException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StreamSource xslStream = new StreamSource(TRANSFORM_FILE);
        Transformer transformer = tFactory.newTransformer(xslStream);
        StreamSource in = new StreamSource(FIRST_FILE);
        StreamResult out = new StreamResult(SECOND_FILE);
        transformer.transform(in,out);

    }
    public long parseXml() throws ParserConfigurationException, IOException, SAXException {
        long sum = 0;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document doc = db.parse(SECOND_FILE);
        NodeList list = doc.getElementsByTagName("entry");
        for(int i=0; i<list.getLength();i++){
            String value = list.item(i).getAttributes().getNamedItem("field").getNodeValue();
            sum += Integer.parseInt(value);
        }
    return sum;
    }

}