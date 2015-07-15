package fr.sii.controller.admin.session;

/**
 * Created by tmaugin on 15/05/2015.
 */

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gdata.util.ServiceException;
import fr.sii.config.application.ApplicationSettings;
import fr.sii.domain.admin.session.AdminViewedSession;
import fr.sii.domain.admin.user.AdminUser;
import fr.sii.domain.exception.NotFoundException;
import fr.sii.domain.spreadsheet.Row;
import fr.sii.domain.spreadsheet.RowResponse;
import fr.sii.service.admin.session.AdminViewedSessionService;
import fr.sii.service.admin.user.AdminUserService;
import fr.sii.service.email.EmailingService;
import fr.sii.service.spreadsheet.SpreadsheetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping(value="api/admin", produces = "application/json; charset=utf-8")
public class AdminSessionController {

    private SpreadsheetService googleService;

    private EmailingService emailingService;

    private ApplicationSettings applicationSettings;

    private AdminUserService adminUserServiceCustom;

    private AdminViewedSessionService adminViewedSessionService;

    public void setAdminViewedSessionService(AdminViewedSessionService adminViewedSessionService) {
        this.adminViewedSessionService = adminViewedSessionService;
    }

    public void setAdminUserServiceCustom(AdminUserService adminUserServiceCustom) {
        this.adminUserServiceCustom = adminUserServiceCustom;
    }

    public void setGoogleService(SpreadsheetService googleService) {
        this.googleService = googleService;
    }

    public void setEmailingService(EmailingService emailingService) {
        this.emailingService = emailingService;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @RequestMapping(value="/sessions", method= RequestMethod.GET)
    @ResponseBody
    public List<RowResponse> getGoogleSpreadsheets() throws IOException, ServiceException, EntityNotFoundException {
        return googleService.getRowsSession();
    }

    @RequestMapping(value="/sessions/ordered", method= RequestMethod.GET)
    @ResponseBody
    public List<Long> getGoogleSpreadsheetsOrderedList() throws IOException, ServiceException, EntityNotFoundException {
        List<Long> ids = new ArrayList<>();
        List<RowResponse> resp = googleService.getRowsSession();
        for(RowResponse row : resp) {
            ids.add(row.getAdded());
        }
        Collections.sort(ids);
        Collections.reverse(ids);
        return ids;
    }

    @RequestMapping(value="/drafts", method= RequestMethod.GET)
    @ResponseBody
    public List<Row> getGoogleSpreadsheetsDraft() throws IOException, ServiceException, EntityNotFoundException {
        return googleService.getRowsDraft();
    }

    @RequestMapping(value="/sessions/{added}", method= RequestMethod.GET)
    @ResponseBody
    public RowResponse getGoogleSpreadsheet(@PathVariable String added) throws IOException, ServiceException, NotFoundException, EntityNotFoundException {
        return googleService.getRow(added);
    }

    @RequestMapping(value="/sessions/{added}", method= RequestMethod.DELETE)
    @ResponseBody
    public void deleteGoogleSpreadsheet(@PathVariable String added) throws IOException, ServiceException, NotFoundException, EntityNotFoundException {
        googleService.deleteRow(added);
    }

    @RequestMapping(value="/sessions/viewed/{added}", method= RequestMethod.POST)
    @ResponseBody
    public AdminViewedSession postGoogleSpreadsheetViewed(@PathVariable String added) throws NotFoundException {
        AdminUser currentUser = adminUserServiceCustom.getCurrentUser();
        if(currentUser == null)
        {
            throw new NotFoundException("User not found");
        }
        return adminViewedSessionService.put(Long.parseLong(added),currentUser.getEntityId(), new AdminViewedSession(Long.parseLong(added), currentUser.getEntityId(), new Date()));
    }
}