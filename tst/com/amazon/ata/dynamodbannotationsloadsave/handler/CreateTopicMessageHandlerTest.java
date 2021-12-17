package com.amazon.ata.dynamodbannotationsloadsave.handler;

import com.amazon.ata.dynamodbannotationsloadsave.cli.DiscussionCliOperation;
import com.amazon.ata.dynamodbannotationsloadsave.cli.DiscussionCliState;
import com.amazon.ata.dynamodbannotationsloadsave.dynamodb.Member;
import com.amazon.ata.dynamodbannotationsloadsave.dynamodb.Topic;
import com.amazon.ata.dynamodbannotationsloadsave.dynamodb.TopicMessage;
import com.amazon.ata.dynamodbannotationsloadsave.dynamodb.TopicMessageDao;
import com.amazon.ata.input.console.ATAUserHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CreateTopicMessageHandlerTest {
    private CreateTopicMessageHandler handler;
    private DiscussionCliState state;

    @Mock
    private ATAUserHandler userHandler;
    @Mock
    private TopicMessageDao topicMessageDao;

    @BeforeEach
    private void setup() {
        initMocks(this);
        handler = new CreateTopicMessageHandler(userHandler, topicMessageDao);
        state = getState();
    }

    @Test
    void handleRequest_userProvidesMessageContent_daoReceivesRequestToCreateTopicMessage() {
        // GIVEN
        // TopicMessage data
        String topicName = "name";
        String author = "author";
        String message = "message";
        // TopicMessage to create
        TopicMessage newTopicMessage = new TopicMessage();
        newTopicMessage.setTopicName(topicName);
        newTopicMessage.setAuthor(author);
        newTopicMessage.setMessageContent(message);
        // State
        state.setCurrentMember(new Member(author));
        state.setCurrentTopic(new Topic(topicName, "description"));
        // User handler returns message
        when(userHandler.getString("Message: ")).thenReturn(message);
        // DAO allows creating topic
        when(topicMessageDao.saveTopicMessage(any(TopicMessage.class))).thenReturn(newTopicMessage);

        // WHEN
        String result = handler.handleRequest(state);

        // THEN
        // DAO allows creating topic message
        verify(topicMessageDao, times(1)).saveTopicMessage(any(TopicMessage.class));
        // Message is displayed
        assertTrue(result.contains(newTopicMessage.getMessageContent()));

    }

    @Test
    void handleRequest_withoutCurrentTopic_nextOperationIsViewTopics() {
        // GIVEN
        // user response
        String message = "abc";
        when(userHandler.getString(anyString())).thenReturn(message);
        // make sure there's a member so no exception thrown
        state.setCurrentMember(new Member("amember"));

        // WHEN
        handler.handleRequest(state);

        // THEN
        assertEquals(DiscussionCliOperation.VIEW_TOPICS, state.getNextOperation());
    }

    @Test
    void handleRequest_afterCreatingTopicMessage_nextOperationIsViewTopicMessages() {
        // GIVEN
        // user response
        String messageContent = "abc";
        when(userHandler.getString(anyString())).thenReturn(messageContent);
        // there is a topic in state, and it has valid topic
        Topic currentTopic = new Topic("An interesting subject", "Or so some say");
        state.setCurrentTopic(currentTopic);
        // there is current member set
        Member currentMember = new Member("ladybugzRUL");
        state.setCurrentMember(currentMember);

        // WHEN
        handler.handleRequest(state);

        // THEN
        assertEquals(DiscussionCliOperation.VIEW_TOPIC_MESSAGES, state.getNextOperation());
    }

    @Test
    void handleRequest_withNullCurrentMemberInState_throwsException() {
        // GIVEN
        // state does not have member but has current topic
        state.setCurrentMember(null);
        Topic currentTopic = new Topic("a lonely lonely topic", "with description");
        state.setCurrentTopic(currentTopic);

        // WHEN + THEN - exception fires
        assertThrows(IllegalStateException.class, () -> handler.handleRequest(state));
    }

    private DiscussionCliState getState() {
        return new DiscussionCliState();
    }
}