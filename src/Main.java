import UDP.UdpClient;
import framesLib.screenables.MyFrame;
import model.SettingsInitializer;
import model.help.Property;
import view.MainPanel;

import java.io.IOException;
import java.util.Properties;

public class Main implements Property {
    public static void main(String[] args) {
        UdpClient.shared.run();

        SettingsInitializer.init(new Main());
        MyFrame frame = new MyFrame();
        frame.changeScreen(new MainPanel());
    }
    public Properties getProperties(String fileName)throws IOException {
        Properties properties = new Properties();
        properties.loadFromXML(Main.class.getResourceAsStream(fileName));
        return properties;
    }
}
