package com.example.testtaskqaibrahimov;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static io.restassured.RestAssured.given;

public class ApiTest {
    @Test
    public void TestCase3(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com/";

        //Get auth token
        String authRequestBody = """
                {
                    "username" : "admin",
                    "password" : "password123"
                }""";

        Response authResponse = given()
                .header("Content-type", "application/json")
                .and()
                .body(authRequestBody)
                .when()
                    .post("/auth")
                .then()
                    .statusCode(200)
                .extract().response();

        String token = authResponse.path("token");

        //Create new booking
        String firstname = "Jim";
        String lastname = "Brown";
        int totalprice = 111;
        boolean depositpaid = true;
        String checkin = "2018-01-01";
        String checkout = "2019-01-01";
        String additionalneeds = "Breakfast";

        String newBookingRequestBody = "{\n" +
                "    \"firstname\" : \""+firstname+"\",\n" +
                "    \"lastname\" : \""+lastname+"\",\n" +
                "    \"totalprice\" : "+totalprice+",\n" +
                "    \"depositpaid\" : "+depositpaid+",\n" +
                "    \"bookingdates\" : {\n" +
                "        \"checkin\" : \""+checkin+"\",\n" +
                "        \"checkout\" : \""+checkout+"\"\n" +
                "    },\n" +
                "    \"additionalneeds\" : \""+additionalneeds+"\"\n" +
                "}";

        Response newBookingResponse = given()
                .header("Content-type", "application/json")
                .header("Cookie", "token="+token) //no need to use token to create booking using POST
                .and()
                .body(newBookingRequestBody)
                .when()
                    .post("/booking")
                .then()
                    .statusCode(200)
                .extract().response();

        int bookingId = newBookingResponse.path("bookingid");
        LinkedHashMap<?, ?> booking = newBookingResponse.path("booking");

        //Verify booking created corect
        Assert.assertEquals(booking.get("firstname"), firstname);
        Assert.assertEquals(booking.get("lastname"), lastname);
        Assert.assertEquals(booking.get("totalprice"), totalprice);
        Assert.assertEquals(booking.get("depositpaid"), depositpaid);
        Assert.assertEquals(((LinkedHashMap<?, ?>)booking.get("bookingdates")).get("checkin"), checkin);
        Assert.assertEquals(((LinkedHashMap<?, ?>)booking.get("bookingdates")).get("checkout"), checkout);
        Assert.assertEquals(booking.get("additionalneeds"), additionalneeds);

        //Update the booking details
        totalprice = 200;
        String updateBookingRequestBody = "{\n" +
                "    \"totalprice\" : \""+ totalprice +"\"\n" +
                "}";

        given()
        .header("Content-type", "application/json")
        .header("Cookie", "token="+token)
        .and()
        .body(updateBookingRequestBody)
        .when()
            .patch("/booking/"+bookingId)
        .then()
            .statusCode(200);

        //Get details of the updated booking, and ensure it has new details.
        Response getBookingResponse = given()
                .header("Content-type", "application/json")
                .and()
                .when()
                    .get("/booking/"+bookingId)
                .then()
                    .statusCode(200)
                .extract().response();

        Assert.assertEquals((Integer) getBookingResponse.path("totalprice"), totalprice);

        //Get all bookings and check them have a newly created booking
        Response getAllBookingResponse = given()
                .header("Content-type", "application/json")
                .and()
                .when()
                    .get("/booking")
                .then()
                    .statusCode(200)
                .extract().response();

        ArrayList<?> bookingIdList = getAllBookingResponse.path("bookingid");
        Assert.assertTrue(bookingIdList.contains(bookingId));

        //Delete the booking.
        given()
                .header("Content-type", "application/json")
                .header("Cookie", "token="+token)
                .and()
                .when()
                .delete("/booking/"+bookingId)
                .then()
                .statusCode(201);

        //Verify booking id is no longer available
        getAllBookingResponse = given()
                .header("Content-type", "application/json")
                .and()
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract().response();

        bookingIdList = getAllBookingResponse.path("bookingid");
        Assert.assertFalse(bookingIdList.contains(bookingId));
    }
}
