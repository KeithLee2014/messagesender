package com.gmail.keith1987;

import com.gmail.keith1987.pojos.Order;
import com.gmail.keith1987.pojos.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;

import java.util.Iterator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by keith on 18/03/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(com.gmail.keith1987.TestConfig.class)
public class MessageSenderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .build();

        Mockito.reset(jmsTemplate);
    }

    String fileContent = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n"+
            "<Order><accont>AX001</accont><SubmittedAt>1507060723641</SubmittedAt><ReceivedAt>1507060723642</ReceivedAt><market>VOD.L</market><action>BUY</action><size>100</size></Order>\n"+
            "<Order><accont>AX002</accont><SubmittedAt>1507060723651</SubmittedAt><ReceivedAt>1507060723652</ReceivedAt><market>VOD.L</market><action>BUY</action><size>200</size></Order>\n";

    @Test
    public void firstLoadPageContainsEmptySettings() throws Exception {

        MvcResult result = mockMvc.perform(get("/messagesender"))
                .andExpect(status().isOk())
                .andReturn();

        Settings settings = (Settings) result.getModelAndView().getModel().get("settings");
        assertTrue(StringUtils.isEmpty(settings.getBroker()));
        assertTrue(StringUtils.isEmpty(settings.getUsername()));
        assertTrue(StringUtils.isEmpty(settings.getPassword()));
        assertTrue(StringUtils.isEmpty(settings.getDestinationName()));
        assertTrue(StringUtils.isEmpty(settings.getMode()));
    }

    @Test
    public void submitFormWithFaultyBrokerShouldShowErrorPage() throws Exception {
        mockMvc.perform(post("/messagesender")
                .param("broker", "shouldcausetcperror")
                .param("username","admin")
                .param("password","manager")
                .param("destination","tralala")
                .param("mode","queue"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"))
                .andReturn();
    }

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    @Test
    public void uploadFileAndSubmitFormShouldSendMessage() throws Exception {
        HttpSession session = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/messagesender/upload")
                .file(new MockMultipartFile("file", "dontcare.xml", "", fileContent.getBytes())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messagesender"))
                .andReturn()
                .getRequest()
                .getSession();

        String DESTINATION = "tralala";

        mockMvc.perform(post("/messagesender").session((MockHttpSession) session)
                .param("broker", "tcp://localhost:61616")
                .param("username","admin")
                .param("password","manager")
                .param("destinationName", DESTINATION)
                .param("mode","queue"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();


        verify(jmsTemplate,times(2)).convertAndSend(Mockito.eq(DESTINATION), orderCaptor.capture());

        Iterator<Order> iterator = orderCaptor.getAllValues().iterator();

        assertThat(iterator.next(), equalTo(new Order("AX001", 1507060723642L, 1507060723641L, "VOD.L", "BUY", 100)));
        assertThat(iterator.next(), equalTo(new Order("AX002", 1507060723652L, 1507060723651L, "VOD.L", "BUY", 200)));

        //Verify sent to queue
        verify(jmsTemplate).setPubSubDomain(false);
    }

    @Test
    public void uploadFileAndSubmitFormShouldSendMessageToTopic() throws Exception {
        HttpSession session = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/messagesender/upload")
                .file(new MockMultipartFile("file", "dontcare.xml", "", fileContent.getBytes())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messagesender"))
                .andReturn()
                .getRequest()
                .getSession();

        String DESTINATION = "tralala";

        mockMvc.perform(post("/messagesender").session((MockHttpSession) session)
                .param("broker", "tcp://localhost:61616")
                .param("username","admin")
                .param("password","manager")
                .param("destinationName", DESTINATION)
                .param("mode","topic"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();


        verify(jmsTemplate,times(2)).convertAndSend(Mockito.eq(DESTINATION), orderCaptor.capture());

        Iterator<Order> iterator = orderCaptor.getAllValues().iterator();

        assertThat(iterator.next(), equalTo(new Order("AX001", 1507060723642L, 1507060723641L, "VOD.L", "BUY", 100)));
        assertThat(iterator.next(), equalTo(new Order("AX002", 1507060723652L, 1507060723651L, "VOD.L", "BUY", 200)));

        //Verify sent to topic
        verify(jmsTemplate).setPubSubDomain(true);
    }

}
