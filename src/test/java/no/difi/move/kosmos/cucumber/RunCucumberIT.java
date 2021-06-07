package no.difi.move.kosmos.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import lombok.experimental.UtilityClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"classpath:no.difi.move.kosmos.cucumber"},
        features = "classpath:cucumber",
        plugin = {"pretty", "json:target/cucumber/cucumber.json"}
)
@UtilityClass
public class RunCucumberIT {
}
