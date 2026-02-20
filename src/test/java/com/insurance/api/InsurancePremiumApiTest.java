package com.insurance.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Insurance Premium Calculation API Tests")
public class InsurancePremiumApiTest {

    private static final String ISO_8601_PATTERN =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*";

    @BeforeAll
    static void setup() {
        String baseUrl = System.getProperty("api.base.url");
        if (baseUrl == null || baseUrl.isEmpty()) {
            try (InputStream input = InsurancePremiumApiTest.class
                    .getClassLoader()
                    .getResourceAsStream("test-config.properties")) {
                if (input != null) {
                    Properties props = new Properties();
                    props.load(input);
                    baseUrl = props.getProperty("api.base.url", "http://localhost:8080");
                }
            } catch (IOException e) {
                baseUrl = "http://localhost:8080";
            }
        }
        if (baseUrl == null) {
            baseUrl = "http://localhost:8080";
        }
        RestAssured.baseURI = baseUrl;
        RestAssured.basePath = "/api/insurance";
    }

    private Map<String, Object> buildRequest(String customerName, String customerAddress, String insuranceType) {
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", customerName);
        request.put("customerAddress", customerAddress);
        request.put("insuranceType", insuranceType);
        return request;
    }

    @Nested
    @DisplayName("Base Premium Calculation Tests")
    class BasePremiumTests {

        @Test
        @DisplayName("AUTO insurance returns base premium of 5000")
        void testAutoInsuranceBasePremium() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("John", "123 Main St", "AUTO"))
            .when()
                .post()
            .then()
                .statusCode(201)
                .body("customerName", equalTo("John"))
                .body("customerAddress", equalTo("123 Main St"))
                .body("insuranceType", equalTo("AUTO"))
                .body("applicationId", notNullValue())
                .body("calculatedPremium", equalTo(5000))
                .body("createdAt", matchesPattern(ISO_8601_PATTERN));
        }

        @Test
        @DisplayName("MEDICAL insurance returns base premium of 7000")
        void testMedicalInsuranceBasePremium() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("John", "123 Main St", "MEDICAL"))
            .when()
                .post()
            .then()
                .statusCode(201)
                .body("customerName", equalTo("John"))
                .body("customerAddress", equalTo("123 Main St"))
                .body("insuranceType", equalTo("MEDICAL"))
                .body("applicationId", notNullValue())
                .body("calculatedPremium", equalTo(7000))
                .body("createdAt", matchesPattern(ISO_8601_PATTERN));
        }

        @Test
        @DisplayName("HOUSE insurance returns base premium of 10000")
        void testHouseInsuranceBasePremium() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("John", "123 Main St", "HOUSE"))
            .when()
                .post()
            .then()
                .statusCode(201)
                .body("customerName", equalTo("John"))
                .body("customerAddress", equalTo("123 Main St"))
                .body("insuranceType", equalTo("HOUSE"))
                .body("applicationId", notNullValue())
                .body("calculatedPremium", equalTo(10000))
                .body("createdAt", matchesPattern(ISO_8601_PATTERN));
        }
    }

    @Nested
    @DisplayName("Premium Modifier Tests")
    class PremiumModifierTests {

        @Test
        @DisplayName("5% discount applied when customer name length exceeds 10 characters")
        void testDiscountOnlyForLongName() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("JohnDoeSmith", "123 Main St", "AUTO"))
            .when()
                .post()
            .then()
                .statusCode(201)
                .body("customerName", equalTo("JohnDoeSmith"))
                .body("customerAddress", equalTo("123 Main St"))
                .body("insuranceType", equalTo("AUTO"))
                .body("applicationId", notNullValue())
                .body("calculatedPremium", equalTo(4750))
                .body("createdAt", matchesPattern(ISO_8601_PATTERN));
        }

        @Test
        @DisplayName("10% surcharge applied when address contains Metro")
        void testSurchargeOnlyForMetroAddress() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("John", "123 Metro Street", "AUTO"))
            .when()
                .post()
            .then()
                .statusCode(201)
                .body("customerName", equalTo("John"))
                .body("customerAddress", equalTo("123 Metro Street"))
                .body("insuranceType", equalTo("AUTO"))
                .body("applicationId", notNullValue())
                .body("calculatedPremium", equalTo(5500))
                .body("createdAt", matchesPattern(ISO_8601_PATTERN));
        }

        @Test
        @DisplayName("Combined discount and surcharge: discount applied first then surcharge")
        void testCombinedDiscountAndSurcharge() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("JohnDoeSmith", "123 Metro Street", "AUTO"))
            .when()
                .post()
            .then()
                .statusCode(201)
                .body("customerName", equalTo("JohnDoeSmith"))
                .body("customerAddress", equalTo("123 Metro Street"))
                .body("insuranceType", equalTo("AUTO"))
                .body("applicationId", notNullValue())
                .body("calculatedPremium", equalTo(5225))
                .body("createdAt", matchesPattern(ISO_8601_PATTERN));
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Returns 400 when customerName is null")
        void testNullCustomerName() {
            Map<String, Object> request = new HashMap<>();
            request.put("customerName", null);
            request.put("customerAddress", "123 Main St");
            request.put("insuranceType", "AUTO");

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Returns 400 when customerName is empty")
        void testEmptyCustomerName() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("", "123 Main St", "AUTO"))
            .when()
                .post()
            .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Returns 400 when customerAddress is null")
        void testNullCustomerAddress() {
            Map<String, Object> request = new HashMap<>();
            request.put("customerName", "John");
            request.put("customerAddress", null);
            request.put("insuranceType", "AUTO");

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Returns 400 when customerAddress is empty")
        void testEmptyCustomerAddress() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("John", "", "AUTO"))
            .when()
                .post()
            .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Returns 400 when insuranceType is null")
        void testNullInsuranceType() {
            Map<String, Object> request = new HashMap<>();
            request.put("customerName", "John");
            request.put("customerAddress", "123 Main St");
            request.put("insuranceType", null);

            given()
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post()
            .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("message", notNullValue());
        }

        @Test
        @DisplayName("Returns 400 when insuranceType is invalid")
        void testInvalidInsuranceType() {
            given()
                .contentType(ContentType.JSON)
                .body(buildRequest("John", "123 Main St", "INVALID"))
            .when()
                .post()
            .then()
                .statusCode(400)
                .body("errorCode", equalTo("VALIDATION_ERROR"))
                .body("message", notNullValue());
        }
    }
}
