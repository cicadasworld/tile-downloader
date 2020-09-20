import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private static Properties prop = null;
    private static final String CONFIG_FILE_LOCATION = "config.properties";

    private Configuration() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_LOCATION);
        prop = new Properties();
        try {
            prop.load(new InputStreamReader(is, "utf-8"));
            is.close();
        } catch (IOException ex) {
            logger.error("Can't find file：config.properties");
        }
    }

    public static Configuration getInstance() {
        return ConfigurationHolder.instance;
    }

    private static class ConfigurationHolder {
        private static final Configuration instance = new Configuration();
    }

    public String getProperty(String property) {
        if (prop == null) {
            throw new RuntimeException("Error Properties is null");
        }

        return prop.getProperty(property);
    }

    public String getString(String property) {
        return getProperty(property);
    }

    public Integer getInt(String property) {
        return Integer.parseInt(getProperty(property));
    }

    public Long getLong(String property) {
        return Long.parseLong(getProperty(property));
    }

    public Boolean getBoolean(String property) {
        return Boolean.parseBoolean(getProperty(property));
    }
}
