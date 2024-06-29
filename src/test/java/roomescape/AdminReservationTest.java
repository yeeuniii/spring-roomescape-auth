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
import roomescape.reservation.ui.dto.AdminReservationRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservationtime.application.ReservationTimeService;
import roomescape.reservationtime.ui.dto.ReservationTimeRequest;
import roomescape.theme.application.ThemeService;
import roomescape.theme.ui.dto.ThemeRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8888"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminReservationTest {
    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private SignUpService signUpService;

    @BeforeEach
    public void setPort() {
        RestAssured.port = 8888;
    }

    private void makeDummyTimeAndTheme() {
        reservationTimeService.add(ReservationTimeRequest.create("13:00"));
        themeService.add(ThemeRequest.create("theme1", "bla", "thumbnail"));
    }

    @Test
    @DisplayName("AdminReservationController - create()")
    void 관리자_예약() {
        String name = "yeeun";
        signUpService.signUp(new MemberRequest(name, "email@email", "password"));
        makeDummyTimeAndTheme();
        String date = LocalDate.now().plusWeeks(1).toString();

        var response = RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(new AdminReservationRequest(date, 1L, 1L, 1L))
                .when().post("/admin/reservations")
                .then().log().all().statusCode(HttpStatus.CREATED.value()).extract().as(ReservationResponse.class);

        assertThat(response.getMemberName()).isEqualTo(name);
    }
}
