package net.clementlevallois.nocodeapp.web.front.functions;

import io.mikael.urlbuilder.UrlBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.annotation.MultipartConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.nocodeapp.web.front.backingbeans.SessionBean;
import net.clementlevallois.nocodeapp.web.front.exportdata.ExportToVosViewer;
import net.clementlevallois.nocodeapp.web.front.backingbeans.ApplicationPropertiesBean;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author LEVALLOIS
 */
@Named
@SessionScoped
@MultipartConfig
public class ConverterBean implements Serializable {

    private UploadedFile uploadedFile;
    private String option = "sourceGexf";
    private String item = "item_name";
    private String link = "link_name";
    private String linkStrength = "link strength name";
    private byte[] inputFileAsByteArray;
    private String uploadButtonMessage;
    private boolean renderGephiWarning = true;

    private byte[] gexfAsByteArray;
    private boolean shareVVPublicly;

    private StreamedContent fileToSave;
    private Properties privateProperties;

    private String dataPersistenceUniqueId = "";
    private String sessionId;

    @Inject
    SessionBean sessionBean;

    @Inject
    ApplicationPropertiesBean applicationProperties;

    public ConverterBean() {
    }

    @PostConstruct
    public void init() {
        sessionBean.setFunction("networkconverter");
        privateProperties = applicationProperties.getPrivateProperties();
        uploadButtonMessage = sessionBean.getLocaleBundle().getString("general.message.choose_gexf_file");
        sessionId = FacesContext.getCurrentInstance().getExternalContext().getSessionId(false);
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String handleFileUpload(FileUploadEvent event) {
        try {
            byte[] readAllBytes = event.getFile().getInputStream().readAllBytes();
            sessionBean.sendFunctionPageReport();
            dataPersistenceUniqueId = UUID.randomUUID().toString().substring(0, 10);
            Path tempFolderRelativePath = applicationProperties.getTempFolderFullPath();
            Path fullPathForFileContainingGexf = Path.of(tempFolderRelativePath.toString(), dataPersistenceUniqueId + "_result");

            String success = sessionBean.getLocaleBundle().getString("general.nouns.success");
            String is_uploaded = sessionBean.getLocaleBundle().getString("general.verb.is_uploaded");
            sessionBean.addMessage(FacesMessage.SEVERITY_INFO, success, event.getFile().getFileName() + " " + is_uploaded + ".");
            try {
                Files.write(fullPathForFileContainingGexf, readAllBytes);
            } catch (IOException ex) {
                System.out.println("ex:" + ex.getMessage());
            }
        } catch (IOException ex) {
            Logger.getLogger(ConverterBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public void gotoVV() {
        String apiPort = privateProperties.getProperty("nocode_api_port");
        String linkToVosViewer = ExportToVosViewer.exportAndReturnLinkForConversionToVV(dataPersistenceUniqueId, apiPort, shareVVPublicly, applicationProperties, item, link, linkStrength);
        if (linkToVosViewer != null && !linkToVosViewer.isBlank()) {
            try {
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                externalContext.redirect(linkToVosViewer);
            } catch (IOException ex) {
                System.out.println("error in ops for export to vv");
            }
        }
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
        if (option.equals("sourceGexf")) {
            setUploadButtonMessage(sessionBean.getLocaleBundle().getString("general.message.choose_gexf_file"));
            renderGephiWarning = true;
        }
        if (option.equals("sourceVV")) {
            setUploadButtonMessage(sessionBean.getLocaleBundle().getString("general.message.choose_vosviewer_file"));
            renderGephiWarning = false;
        }
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkStrength() {
        return linkStrength;
    }

    public void setLinkStrength(String linkStrength) {
        this.linkStrength = linkStrength;
    }

    public String getUploadButtonMessage() {
        return uploadButtonMessage;
    }

    public void setUploadButtonMessage(String uploadButtonMessage) {
        this.uploadButtonMessage = uploadButtonMessage;
    }

    public boolean isShareVVPublicly() {
        return shareVVPublicly;
    }

    public void setShareVVPublicly(boolean shareVVPublicly) {
        this.shareVVPublicly = shareVVPublicly;
    }

    public boolean isRenderGephiWarning() {
        return renderGephiWarning;
    }

    public void setRenderGephiWarning(boolean renderGephiWarning) {
        this.renderGephiWarning = renderGephiWarning;
    }

    public StreamedContent getFileToSave() {
        StreamedContent fileStream = null;
        try {
            if (inputFileAsByteArray == null) {
                System.out.println("no file found for conversion to gephi");
                return null;
            }

            HttpRequest request;
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(10)).build();

            HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(inputFileAsByteArray);

            URI uri = UrlBuilder
                    .empty()
                    .withScheme("http")
                    .withPort((Integer.valueOf(privateProperties.getProperty("nocode_api_port"))))
                    .withHost("localhost")
                    .withPath("api/convert2gexf")
                    .toUri();

            request = HttpRequest.newBuilder()
                    .POST(bodyPublisher)
                    .uri(uri)
                    .build();

            HttpResponse<byte[]> resp = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() == 200) {
                gexfAsByteArray = resp.body();
            } else {
                gexfAsByteArray = null;
            }

            if (gexfAsByteArray == null) {
                System.out.println("gexfAsByteArray returned by the API was not a 200 code");
                String error = sessionBean.getLocaleBundle().getString("general.nouns.error");
                sessionBean.addMessage(FacesMessage.SEVERITY_ERROR, error, error);
                return null;
            }

            InputStream inputStreamToSave = new ByteArrayInputStream(gexfAsByteArray);
            fileStream = DefaultStreamedContent.builder()
                    .name("results.gexf")
                    .contentType("application/gexf+xml")
                    .stream(() -> inputStreamToSave)
                    .build();

        } catch (IOException | InterruptedException ex) {
            System.out.println("ex:" + ex.getMessage());
        }
        return fileStream;
    }

    public void setFileToSave(StreamedContent fileToSave) {
        this.fileToSave = fileToSave;
    }
}
