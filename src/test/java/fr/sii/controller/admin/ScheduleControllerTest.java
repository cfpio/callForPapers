package fr.sii.controller.admin;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;

import fr.sii.Application;
import fr.sii.dto.TalkUser;
import fr.sii.dto.user.CospeakerProfil;
import fr.sii.dto.user.Schedule;
import fr.sii.dto.user.UserProfil;
import fr.sii.entity.Talk;
import fr.sii.service.TalkUserService;
import fr.sii.service.email.EmailingService;

/**
 * Created by Nicolas on 30/01/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ScheduleControllerTest {

    public static final String JOHN_DOE_EMAIL = "john.doe@gmail.com";

    @Mock
    private TalkUserService talkUserService;

    @Mock
    private EmailingService emailingService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ScheduleController scheduleController = new ScheduleController(talkUserService, emailingService);
        // this.restMockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
        RestAssuredMockMvc.standaloneSetup(scheduleController);
    }

    @Test
    public void testGetScheduleList() throws Exception {

        // Given
        UserProfil userProfil = new UserProfil("John", "Doe");
        // TalkUser 1
        TalkUser talkUser1 = new TalkUser();
        talkUser1.setId(1);
        talkUser1.setName("A talk 1");
        talkUser1.setDescription("A description");
        talkUser1.setSpeaker(userProfil);

        // TalkUser 2
        TalkUser talkUser2 = new TalkUser();
        talkUser2.setId(2);
        talkUser2.setName("A talk 2");
        talkUser2.setDescription("A description");
        talkUser2.setSpeaker(userProfil);

        CospeakerProfil cospeakerProfil1 = new CospeakerProfil();
        cospeakerProfil1.setId(1);
        cospeakerProfil1.setFirstname("Johnny");
        cospeakerProfil1.setLastname("Deep");

        CospeakerProfil cospeakerProfil2 = new CospeakerProfil();
        cospeakerProfil2.setId(2);
        cospeakerProfil2.setFirstname("Alain");
        cospeakerProfil2.setLastname("Connu");

        Set<CospeakerProfil> cospeakerProfils = new HashSet<>();
        cospeakerProfils.add(cospeakerProfil1);
        cospeakerProfils.add(cospeakerProfil2);
        talkUser2.setCospeaker(cospeakerProfils);

        // TalkUser 3
        TalkUser talkUser3 = new TalkUser();
        talkUser3.setId(3);
        talkUser3.setName("A talk 3");
        // no description
        talkUser3.setSpeaker(userProfil);

        List<TalkUser> talkList = new ArrayList<>();
        talkList.add(talkUser1);
        talkList.add(talkUser2);
        talkList.add(talkUser3);

        when(talkUserService.findAll(Talk.State.CONFIRMED)).thenReturn(talkList);

        MockMvcResponse mockMvcResponse = given().contentType("application/json").when().get("/api/admin/scheduledtalks/confirmed");

        System.out.println(mockMvcResponse.asString());

        mockMvcResponse.then().statusCode(200).body("size()", equalTo(3)).body("[0].speakers", equalTo("John Doe"))
                .body("[1].speakers", containsString("John Doe")).body("[1].speakers", containsString("Johnny Deep"))
                .body("[1].speakers", containsString("Alain Connu")).body("[2].speakers", equalTo("John Doe"));
    }

    @Test
    public void testGetSpeakerList() throws Exception {

        // Given
        UserProfil userProfil = new UserProfil("John", "Doe");
        // TalkUser 1
        TalkUser talkUser1 = new TalkUser();
        talkUser1.setId(1);
        talkUser1.setName("A talk 1");
        talkUser1.setDescription("A description");
        talkUser1.setSpeaker(userProfil);

        // TalkUser 2
        TalkUser talkUser2 = new TalkUser();
        talkUser2.setId(2);
        talkUser2.setName("A talk 2");
        talkUser2.setDescription("A description");
        talkUser2.setSpeaker(userProfil);

        CospeakerProfil cospeakerProfil1 = new CospeakerProfil();
        cospeakerProfil1.setId(1);
        cospeakerProfil1.setFirstname("Johnny");
        cospeakerProfil1.setLastname("Deep");

        CospeakerProfil cospeakerProfil2 = new CospeakerProfil();
        cospeakerProfil2.setId(2);
        cospeakerProfil2.setFirstname("Alain");
        cospeakerProfil2.setLastname("Connu");

        Set<CospeakerProfil> cospeakerProfils = new HashSet<>();
        cospeakerProfils.add(cospeakerProfil1);
        cospeakerProfils.add(cospeakerProfil2);
        talkUser2.setCospeaker(cospeakerProfils);

        // TalkUser 3
        TalkUser talkUser3 = new TalkUser();
        talkUser3.setId(3);
        talkUser3.setName("A talk 3");
        // no description
        talkUser3.setSpeaker(userProfil);

        List<TalkUser> talkList = new ArrayList<>();
        talkList.add(talkUser1);
        talkList.add(talkUser2);
        talkList.add(talkUser3);

        when(talkUserService.findAll(Talk.State.ACCEPTED)).thenReturn(talkList);

        MockMvcResponse mockMvcResponse = given().when().get("/api/admin/scheduledtalks/accepted/speakers");

        System.out.println(mockMvcResponse.asString());

        mockMvcResponse.then().statusCode(200).body("size()", equalTo(1));
    }

    @Test
    public void testPutScheduleListWithSendMail() {
        // Given
        UserProfil userProfil = new UserProfil("John", "Doe");
        // TalkUser 1
        TalkUser talkUser1 = new TalkUser();
        talkUser1.setId(1);
        talkUser1.setName("A talk 1");
        talkUser1.setDescription("A description");
        talkUser1.setSpeaker(userProfil);

        CospeakerProfil cospeakerProfil1 = new CospeakerProfil();
        cospeakerProfil1.setId(1);
        cospeakerProfil1.setFirstname("Johnny");
        cospeakerProfil1.setLastname("Deep");

        Set<CospeakerProfil> cospeakerProfils = new HashSet<>();
        cospeakerProfils.add(cospeakerProfil1);
        talkUser1.setCospeaker(cospeakerProfils);

        List<TalkUser> talkList = new ArrayList<>();
        talkList.add(talkUser1);

        List<Schedule> scheduleList = new ArrayList<>();
        Schedule schedule = new Schedule(1, "John", "Doe");
        schedule.setEventStart("2016-02-19T19:35:45.977");
        scheduleList.add(schedule);

        LocalDateTime dateEventStart = LocalDateTime.parse(schedule.getEventStart(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        when(talkUserService.findAll(Talk.State.ACCEPTED, Talk.State.CONFIRMED, Talk.State.REFUSED)).thenReturn(talkList);
        when(talkUserService.updateConfirmedTalk(1, dateEventStart)).thenReturn(talkUser1);
        Mockito.doNothing().when(emailingService).sendEmail(anyString(), anyString(), anyString(), isNull(List.class), isNull(List.class));

        MockMvcResponse mockMvcResponse = given().body(scheduleList).contentType("application/json").put("/api/admin/scheduledtalks?sendMail=true");

        mockMvcResponse.then().statusCode(200);

        verify(emailingService).sendSelectionned(talkUser1, Locale.FRENCH);
    }

    @Test
    public void testPutScheduleListWithoutSendMail() {
        // Given
        UserProfil userProfil = new UserProfil("John", "Doe");
        // TalkUser 1
        TalkUser talkUser1 = new TalkUser();
        talkUser1.setId(1);
        talkUser1.setName("A talk 1");
        talkUser1.setDescription("A description");
        talkUser1.setSpeaker(userProfil);

        CospeakerProfil cospeakerProfil1 = new CospeakerProfil();
        cospeakerProfil1.setId(1);
        cospeakerProfil1.setFirstname("Johnny");
        cospeakerProfil1.setLastname("Deep");

        Set<CospeakerProfil> cospeakerProfils = new HashSet<>();
        cospeakerProfils.add(cospeakerProfil1);
        talkUser1.setCospeaker(cospeakerProfils);

        List<TalkUser> talkList = new ArrayList<>();
        talkList.add(talkUser1);

        List<Schedule> scheduleList = new ArrayList<>();
        Schedule schedule = new Schedule(1, "John", "Doe");
        schedule.setEventStart("2016-02-19T19:35:45.977");
        scheduleList.add(schedule);

        LocalDateTime dateEventStart = LocalDateTime.parse(schedule.getEventStart(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        when(talkUserService.findAll(Talk.State.ACCEPTED, Talk.State.CONFIRMED, Talk.State.REFUSED)).thenReturn(talkList);
        when(talkUserService.updateConfirmedTalk(1, dateEventStart)).thenReturn(talkUser1);
        Mockito.doNothing().when(emailingService).sendEmail(anyString(), anyString(), anyString(), isNull(List.class), isNull(List.class));

        MockMvcResponse mockMvcResponse = given().body(scheduleList).contentType("application/json").put("/api/admin/scheduledtalks");

        mockMvcResponse.then().statusCode(200);

        verifyZeroInteractions(emailingService);
    }

    @Test
    public void testNotifyScheduling() {
        // Given
        UserProfil userProfil = new UserProfil("John", "Doe");
        // TalkUser 1
        TalkUser talkUser1 = new TalkUser();
        talkUser1.setId(1);
        talkUser1.setName("A talk 1");
        talkUser1.setDescription("A description");
        talkUser1.setSpeaker(userProfil);

        CospeakerProfil cospeakerProfil1 = new CospeakerProfil();
        cospeakerProfil1.setId(1);
        cospeakerProfil1.setFirstname("Johnny");
        cospeakerProfil1.setLastname("Deep");

        Set<CospeakerProfil> cospeakerProfils = new HashSet<>();
        cospeakerProfils.add(cospeakerProfil1);
        talkUser1.setCospeaker(cospeakerProfils);

        List<TalkUser> talkList = new ArrayList<>();
        talkList.add(talkUser1);

        when(talkUserService.findAll(Talk.State.ACCEPTED)).thenReturn(talkList);
        Mockito.doNothing().when(emailingService).sendEmail(anyString(), anyString(), anyString(), isNull(List.class), isNull(List.class));

        MockMvcResponse mockMvcResponse = given().contentType("application/json").post("/api/admin/scheduledtalks/notification");

        mockMvcResponse.then().statusCode(200);

        verify(emailingService).sendSelectionned(talkUser1, Locale.FRENCH);
    }
}