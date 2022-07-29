package com.amazon.ata.dynamodbannotationsloadsave.handler;

import com.amazon.ata.dynamodbannotationsloadsave.cli.DiscussionCliOperation;
import com.amazon.ata.dynamodbannotationsloadsave.cli.DiscussionCliState;
import com.amazon.ata.dynamodbannotationsloadsave.dynamodb.Member;
import com.amazon.ata.dynamodbannotationsloadsave.dynamodb.MemberDao;
import com.amazon.ata.dynamodbannotationsloadsave.exceptions.MemberNotFoundException;
import com.amazon.ata.input.console.ATAUserHandler;

/**
 * Handles the request to login (or create) a member.
 */
public class LoginHandler implements DiscussionCliOperationHandler {
    private MemberDao memberDao;
    private ATAUserHandler userHandler;

    /**
     * Constructs a new LoginHandler with provided DAO.
     * @param memberDao the DAO for accessing members
     * @param userHandler Handler to manage user interactions
     */
    public LoginHandler(MemberDao memberDao, ATAUserHandler userHandler) {
        this.memberDao = memberDao;
        this.userHandler = userHandler;
    }

    @Override
    public String handleRequest(DiscussionCliState state) {
        String username = requestUsername();
        Member member = findOrCreateMember(username);

        state.setCurrentMember(member);
        state.setNextOperation(DiscussionCliOperation.VIEW_TOPICS);

        return renderResponse(member);
    }

    private String requestUsername() {
        return userHandler.getString("", "username: ");
    }

    private Member findOrCreateMember(String username) {
        Member member;
        try{
            member = memberDao.getMember(username);
        } catch(MemberNotFoundException ex){
            System.out.println("Member doesn't exist. Creating a new user...");
            member = new Member(username);
            memberDao.createMember(member);
        }
        return member;
    }

    private String renderResponse(Member member) {
        return String.format("Welcome, %s!", member.display());
    }
}
