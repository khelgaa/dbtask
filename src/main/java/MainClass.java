import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;


public class MainClass {

    public static void main(String[] args) throws SQLException, ParserConfigurationException, IOException {
        BaseClass baseClass = null;
        long sum = 0;
        try {
            baseClass = new BaseClass();
            baseClass.setN(100);

            baseClass.insertData();
            baseClass.createFirstFile();
           baseClass.createSecondFile();
            sum = baseClass.parseXml();

           if(baseClass.getConnection() != null)
                baseClass.getConnection().close();
        } catch (IOException | SQLException | ParserConfigurationException e) {
        e.printStackTrace();
    } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Sum = " + sum );
    }
}