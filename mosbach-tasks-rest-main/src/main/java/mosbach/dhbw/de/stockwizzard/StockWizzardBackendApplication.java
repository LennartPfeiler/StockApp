package mosbach.dhbw.de.cftestbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class StockWizzardBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockWizzardBackendApplication.class, args);
    }

}
