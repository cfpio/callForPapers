package io.cfp.api;

import io.cfp.mapper.CommentMapper;
import io.cfp.mapper.ProposalMapper;
import io.cfp.mapper.UserMapper;
import io.cfp.model.Comment;
import io.cfp.model.Proposal;
import io.cfp.model.Role;
import io.cfp.model.User;
import io.cfp.model.queries.CommentQuery;
import io.cfp.service.email.EmailingService;
import io.cfp.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(CommentsController.class)
public class CommentsControllerTest {

    @MockBean
    private CommentMapper commentMapper;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private ProposalMapper proposalMapper;

    @MockBean
    private EmailingService emailingService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void should_get_comments_of_its_proposals_when_user_is_speaker() throws Exception {

        List<Comment> comments = new ArrayList<>();

        Comment comment = new Comment()
            .setId(10)
            .setComment("COMMENT");

        comments.add(comment);

        when(commentMapper.findAll(any(CommentQuery.class))).thenReturn(comments);

        User user = new User();
        user.setEmail("EMAIL");
        user.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);

        Proposal proposal = new Proposal();
        proposal.setSpeaker(user);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        mockMvc.perform(get("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$[0].id").value("10"))
        ;
    }

    @Test
    public void should_get_comments_of_its_proposals_when_user_is_cospeaker() throws Exception {

        List<Comment> comments = new ArrayList<>();

        Comment comment = new Comment()
            .setId(10)
            .setComment("COMMENT");

        comments.add(comment);

        when(commentMapper.findAll(any(CommentQuery.class))).thenReturn(comments);

        User speaker = new User();
        speaker.setId(1);
        speaker.setEmail("EMAIL_SPEAKER");
        speaker.addRole(Role.AUTHENTICATED);

        User cospeaker = new User();
        cospeaker.setId(2);
        cospeaker.setEmail("EMAIL_COSPEAKER");
        cospeaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(cospeaker);

        when(userMapper.findByEmail("EMAIL_COSPEAKER")).thenReturn(cospeaker);

        Proposal proposal = new Proposal();
        proposal.setSpeaker(speaker);
        proposal.getCospeakers().add(cospeaker);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        mockMvc.perform(get("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$[0].id").value("10"))
        ;
    }

    @Test
    public void should_get_all_comments_when_user_is_reviewers() throws Exception {

        List<Comment> comments = new ArrayList<>();

        Comment comment = new Comment()
            .setId(10)
            .setComment("COMMENT");

        comments.add(comment);

        when(commentMapper.findAll(any(CommentQuery.class))).thenReturn(comments);

        User user = new User();
        user.setEmail("EMAIL");
        user.addRole(Role.REVIEWER);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);

        Proposal proposal = new Proposal();
        proposal.setSpeaker(new User().setId(21));
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        mockMvc.perform(get("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$[0].id").value("10"))
        ;
    }

    @Test
    public void should_not_authorized_authenticated_users_to_get_comments_of_other_proposals() throws Exception {

        List<Comment> comments = new ArrayList<>();

        Comment comment = new Comment()
            .setId(10)
            .setComment("COMMENT");

        comments.add(comment);

        when(commentMapper.findAll(any(CommentQuery.class))).thenReturn(comments);

        User user = new User();
        user.setEmail("EMAIL");
        user.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(new User().setId(21));

        Proposal proposal = new Proposal();
        proposal.setSpeaker(user);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        mockMvc.perform(get("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    public void should_not_authorise_anonymous_to_create_comments() throws Exception {

        String newComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(newComment)
        )
            .andDo(print())
            .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    public void a_speaker_should_create_comments() throws Exception {

        User user = new User();
        user.setId(20);
        user.setEmail("EMAIL");
        user.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(new Proposal().setSpeaker(new User().setId(20)));

        String newComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(newComment)
        )
            .andDo(print())
            .andExpect(status().isCreated())
        ;
    }

    @Test
    public void a_cospeaker_should_create_comments() throws Exception {

        User user = new User();
        user.setId(20);
        user.setEmail("EMAIL");
        user.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);

        Proposal proposal = new Proposal();
        proposal.setSpeaker(new User().setId(10));
        proposal.getCospeakers().add(new User().setId(20));
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        String newComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(newComment)
        )
            .andDo(print())
            .andExpect(status().isCreated())
        ;
    }

    @Test
    public void should_authorize_reviewer_to_create_comments_on_all_proposals() throws Exception {

        User user = new User();
        user.setId(20);
        user.setEmail("EMAIL");
        user.addRole(Role.REVIEWER);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(new Proposal().setSpeaker(new User().setId(21)));

        String newComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(newComment)
        )
            .andDo(print())
            .andExpect(status().isCreated())
        ;
    }

    @Test
    public void should_not_authorize_user_to_create_comments_on_other_proposals() throws Exception {

        User user = new User();
        user.setId(20);
        user.setEmail("EMAIL");
        user.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(new Proposal().setSpeaker(new User().setId(21)));

        String newComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(newComment)
        )
            .andDo(print())
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    public void a_speaker_should_update_his_comments() throws Exception {

        User speaker = new User();
        speaker.setId(20);
        speaker.setEmail("EMAIL");
        speaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(speaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(speaker);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(speaker));

        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(speaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        String updatedComment = Utils.getContent("/json/comments/update_comment.json");

        mockMvc.perform(put("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isNoContent())
        ;
    }

    @Test
    public void a_cospeaker_should_update_his_comments() throws Exception {

        User cospeaker = new User();
        cospeaker.setId(22);
        cospeaker.setEmail("EMAIL");
        cospeaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(cospeaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(cospeaker);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(new User().setId(21));
        proposal.getCospeakers().add(cospeaker);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(cospeaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        String updatedComment = Utils.getContent("/json/comments/update_comment.json");

        mockMvc.perform(put("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isNoContent())
        ;
    }

    @Test
    public void a_cospeaker_should_not_update_another_speaker_comments() throws Exception {

        User cospeaker = new User();
        cospeaker.setId(22);
        cospeaker.setEmail("EMAIL");
        cospeaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(cospeaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(cospeaker);
        User speaker = new User().setId(21);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(speaker);
        proposal.getCospeakers().add(cospeaker);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);
        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(speaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        String updatedComment = Utils.getContent("/json/comments/update_comment.json");

        mockMvc.perform(put("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    public void a_speaker_should_delete_his_comments() throws Exception {

        User speaker = new User();
        speaker.setId(20);
        speaker.setEmail("EMAIL");
        speaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(speaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(speaker);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(speaker));

        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(speaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        mockMvc.perform(delete("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isNoContent())
        ;
    }

    @Test
    public void a_cospeaker_should_delete_his_comments() throws Exception {

        User cospeaker = new User();
        cospeaker.setId(22);
        cospeaker.setEmail("EMAIL");
        cospeaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(cospeaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(cospeaker);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(new User().setId(21));
        proposal.getCospeakers().add(cospeaker);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(cospeaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        mockMvc.perform(delete("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isNoContent())
        ;
    }

    @Test
    public void a_cospeaker_should_not_delete_another_speaker_comments() throws Exception {

        User cospeaker = new User();
        cospeaker.setId(22);
        cospeaker.setEmail("EMAIL");
        cospeaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(cospeaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(cospeaker);
        User speaker = new User().setId(21);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(speaker);
        proposal.getCospeakers().add(cospeaker);

        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);
        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(speaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        mockMvc.perform(delete("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
        )
            .andDo(print())
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    public void should_email_admins_if_internal_comment_is_updated_by_admin() throws Exception {

        User user = new User();
        user.setId(20);
        user.setEmail("EMAIL");
        user.addRole(Role.REVIEWER);
        String token = Utils.createTokenForUser(user);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(new User().setId(21));

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        String updatedComment = Utils.getContent("/json/comments/update_internal_comment.json");

        mockMvc.perform(put("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isNoContent())
        ;

        verify(emailingService).sendNewCommentToAdmins(eq(user), eq(proposal), anyString());
    }

    @Test
    public void should_email_admins_if_comment_is_updated_by_speaker() throws Exception {

        User speaker = new User();
        speaker.setId(20);
        speaker.setEmail("EMAIL");
        speaker.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(speaker);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(speaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(speaker);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        Comment comment = new Comment();
        comment.setId(10);
        comment.setUser(speaker);
        when(commentMapper.findById(eq(10), anyString())).thenReturn(comment);

        String updatedComment = Utils.getContent("/json/comments/update_comment.json");

        mockMvc.perform(put("/api/proposals/25/comments/10")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isNoContent())
        ;

        verify(emailingService).sendNewCommentToAdmins(eq(speaker), eq(proposal), anyString());
    }

    @Test
    public void should_email_admins_if_internal_comment_is_created_by_admin() throws Exception {

        User user = new User();
        user.setId(20);
        user.setEmail("EMAIL");
        user.addRole(Role.REVIEWER);
        String token = Utils.createTokenForUser(user);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(new User().setId(21));

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        String updatedComment = Utils.getContent("/json/comments/new_internal_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isCreated())
        ;

        verify(emailingService).sendNewCommentToAdmins(eq(user), eq(proposal), anyString());
    }

    @Test
    public void should_email_speaker_if_comment_is_public_and_created_by_admins() throws Exception {

        User user = new User();
        user.setId(21);
        user.setEmail("EMAIL");
        user.addRole(Role.REVIEWER);
        String token = Utils.createTokenForUser(user);
        User speaker = new User().setId(22);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(speaker);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        String updatedComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isCreated())
        ;

        verify(emailingService).sendNewCommentToSpeaker(eq(speaker), eq(proposal), anyString());
    }

    @Test
    public void should_email_admins_if_comment_is_created_by_speaker() throws Exception {

        User user = new User();
        user.setId(21);
        user.setEmail("EMAIL");
        user.addRole(Role.AUTHENTICATED);
        String token = Utils.createTokenForUser(user);
        Proposal proposal = new Proposal().setId(25)
            .setName("PROPOSAL_NAME")
            .setSpeaker(user);

        when(userMapper.findByEmail("EMAIL")).thenReturn(user);
        when(proposalMapper.findById(eq(25), anyString())).thenReturn(proposal);

        String updatedComment = Utils.getContent("/json/comments/new_comment.json");

        mockMvc.perform(post("/api/proposals/25/comments")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header("Authorization", "Bearer "+token)
            .content(updatedComment)
        )
            .andDo(print())
            .andExpect(status().isCreated())
        ;

        verify(emailingService).sendNewCommentToAdmins(eq(user), eq(proposal), anyString());
    }

}
