package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.application.SignUpService;
import roomescape.member.ui.dto.MemberRequest;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.reservation.ui.dto.ReservationResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8888"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberTest {
    @Autowired
    private SignUpService signUpService;

    @BeforeEach
    public void setPort() {
        RestAssured.port = 8888;
    }

    @Test
    @DisplayName("MemberController - readAll()")
    void 전체_사용자_조회() {
        signUpService.signUp(new MemberRequest("yeeun", "anna862700@gmail.com", "password"));
        signUpService.signUp(new MemberRequest("asdf", "asdf@gmail.com", "password"));

        var response = RestAssured
                .given().log().all()
                .when().get("/members")
                .then().log().all().statusCode(HttpStatus.OK.value()).extract();

        assertThat(response.jsonPath().getList("", MemberResponse.class)).hasSize(2);
    }

    @Test
    @DisplayName("MemberController - readAll() non-member")
    void 사용자_없는_경우_전체_사용자_조회() {
        var response = RestAssured
                .given().log().all()
                .when().get("/members")
                .then().log().all().statusCode(HttpStatus.OK.value()).extract();

        assertThat(response.jsonPath().getList("", ReservationResponse.class)).hasSize(0);
    }


    @Test
    @DisplayName("MemberController - readOne()")
    void 단일_사용자_조회() {
        signUpService.signUp(new MemberRequest("yeeun", "anna862700@gmail.com", "password"));
        signUpService.signUp(new MemberRequest("asdf", "asdf@gmail.com", "password"));

        var response = RestAssured
                .given().log().all()
                .when().get("/members/2")
                .then().log().all().statusCode(HttpStatus.OK.value())
                .extract().as(MemberResponse.class);

        assertThat(response.name()).isEqualTo("asdf");
        assertThat(response.email()).isEqualTo("asdf@gmail.com");
    }

    @Test
    @DisplayName("MemberController - readOne() non-existent member")
    void 존재하지_않는_단일_사용자_조회() {
        signUpService.signUp(new MemberRequest("yeeun", "anna862700@gmail.com", "password"));
        signUpService.signUp(new MemberRequest("asdf", "asdf@gmail.com", "password"));

        RestAssured.given().log().all()
                .when().get("/members/3")
                .then().log().all().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("MemberController - create()")
    void 사용자_생성() {
        String name = "yeeun";
        String email = "anna862700@gmail.com";
        String password = "password";

        var response = RestAssured
                .given().log().all()
                .body(new MemberRequest(name, email, password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then().log().all().statusCode(HttpStatus.CREATED.value()).extract();

        MemberResponse body = response.body().as(MemberResponse.class);
        assertThat(body.name()).isEqualTo(name);
        assertThat(body.email()).isEqualTo(email);
        String location = response.header("location");
        assertThat(location).isEqualTo("/members/" + body.id());

        MemberResponse member = RestAssured
                .given().log().all()
                .when().get(location)
                .then().log().all().statusCode(HttpStatus.OK.value()).extract().as(MemberResponse.class);

        assertThat(member.name()).isEqualTo(name);
        assertThat(member.email()).isEqualTo(email);
    }

    @Test
    @DisplayName("MemberController - create() duplicated email")
    void 이미_사용하고_있는_이메일로_사용자_생성() {
        String name = "yeeun";
        String email = "anna862700@gmail.com";
        String password = "password";
        사용자_생성();

        var response = RestAssured
                .given().log().all()
                .body(new MemberRequest(name, email, password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then().log().all().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
