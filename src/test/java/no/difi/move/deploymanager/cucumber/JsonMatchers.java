package no.difi.move.deploymanager.cucumber;

import lombok.experimental.UtilityClass;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

@UtilityClass
class JsonMatchers {

    static Matcher<String> json(
            final String expectedJSON, JSONCompareMode mode) {

        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String actualJSON) {
                try {
                    JSONCompareResult result = JSONCompare.compareJSON(expectedJSON, actualJSON, mode);
                    return !result.failed();
                } catch (JSONException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText(" JSON ")
                        .appendValue(expectedJSON)
                        .appendText(" that is ");
            }
        };
    }
}
