package com.gmail.keith1987.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gmail.keith1987.pojos.Order;
import com.gmail.keith1987.pojos.Settings;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/")
public class MessageSenderController {

    public MessageSenderController() {}

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQProperties activeMQProperties;

    @Autowired
    private XmlMapper xmlMapper;

    @GetMapping("/")
    public String redirect(){
        return "redirect:/messagesender";
    }

    //Return the message sender form
    @GetMapping("/messagesender")
    public String showForm(Model model, @ModelAttribute Settings settings) {

        if(settings == null)
            settings = new Settings();

        model.addAttribute("settings", settings);
        return "messagesender";
    }

    /***
     * Processes the submission of form.
     * Note that by this time, this method expects that the xml file has been uploaded (which is saved onto /tmp/$session-id).
     * If the xml file has not been uploaded, an error page is shown to the user.
     *
     * @param settings Model containing settings e.g. Broker, username, password, destination and queue/topic
     * @param redirectAttributes
     * @param httpSession HttpSession used to get sessionId, which points to where uploaded xml file resides.
     * @return Error page if i) settings are invalid e.g. bad URI for broker ii) File is not found iii) Any error due to connection/ActiveMQ config
     * MessageSenderPage if message sent successfully.
     */
    @PostMapping("/messagesender")
    public String sendmessage(Model model, @ModelAttribute Settings settings, RedirectAttributes redirectAttributes, HttpSession httpSession) {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    settings.getUsername(),
                    settings.getPassword(),
                    new URI(settings.getBroker().trim())
            );
            jmsTemplate.setConnectionFactory(
                    connectionFactory);

            if("queue".equals(settings.getMode())){
                jmsTemplate.setPubSubDomain(false);
            } else if ("topic".equals(settings.getMode())){
                jmsTemplate.setPubSubDomain(true);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "Problem in URI, please check. " + e.getMessage());
            return "redirect:/error";
        }

        Path uploadedFile = getUploadFilePath(httpSession);
        if( ! Files.exists(uploadedFile)) {
            redirectAttributes.addFlashAttribute("errormessage", "File does not exist :( Please upload.");
            return "redirect:/error";
        }

        try {
            String xmlContent = new String(Files.readAllBytes(uploadedFile));

            //Splits by newline, as stated in the requirements in the email
            List<String> lines = Arrays.asList(xmlContent.split("\\r?\\n"));

            Iterator<String> iterator = lines.iterator();

            //This is the xml tag portion: <?xml version='1.0' encoding='UTF-8' standalone='yes'?>
            iterator.next();//Ignore first line of XML

            //Now we're processing <Order>...</Order>
            while(iterator.hasNext()){
                String line = iterator.next();
                Order order = xmlMapper.readValue(line, Order.class);

                jmsTemplate.convertAndSend(settings.getDestinationName(), order);
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "Message sending failed. Please check credentials and broker url. " + e.getMessage());
            return "redirect:/error";
        }

        model.addAttribute("message", "MESSAGE SENT SUCCESSFULLY!");

        return "messagesender";
    }

    /***
     * Processes the uploading of file. File is saved onto /tmp/$session-id
     * This staggered approach allow sendMessage to be independent of this upload routine.
     * DOES NOT DO FILE VERIFICATION
     * @param file
     * @param redirectAttributes
     * @param httpSession
     * @return Error page if there is an an exception in reading the uploaded file.
     * messagesender page if upload successful, showing what file has been uploaded.
     */
    @PostMapping("/messagesender/upload")
    public String upload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, HttpSession httpSession){
        if(file.isEmpty()){
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:/messagesender";
        }

        try {
            byte[] bytes = file.getBytes();

            Path path = getUploadFilePath(httpSession);
            Files.write(path, bytes);

            redirectAttributes.addFlashAttribute("message", "Upload success! " + file.getOriginalFilename() + " received.");
            return "redirect:/messagesender";
        } catch (IOException e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "Failure in reading ");
            return "redirect:/error";
        }
    }

    private Path getUploadFilePath(HttpSession httpSession) {
        return Paths.get("/tmp/" + httpSession.getId());
    }
}
