package net.clementlevallois.nocodeapp.web.front.functions;

import io.mikael.urlbuilder.UrlBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;
import jakarta.servlet.annotation.MultipartConfig;
import java.util.ArrayList;
import java.util.Properties;
import net.clementlevallois.nocodeapp.web.front.backingbeans.SessionBean;
import net.clementlevallois.nocodeapp.web.front.utils.GEXFSaver;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import net.clementlevallois.functions.model.Prediction;
import net.clementlevallois.nocodeapp.web.front.backingbeans.SingletonBean;

/**
 *
 * @author LEVALLOIS
 */
@Named
@SessionScoped
@MultipartConfig
public class LinkPredictionBean implements Serializable {

    private UploadedFile uploadedFile;
    private byte[] uploadedFileAsByteArray;
    private String uploadButtonMessage;
    private boolean renderGephiWarning = true;
    private int nbPredictions = 1;
    private JsonObject jsonObjectReturned;
    private String augmentedGexf;
    private List<Prediction> topPredictions;
    private Prediction selectedLink;

    private final Properties privateProperties;
    
    
    private StreamedContent fileToSave;

    private boolean success = false;

    @Inject
    SessionBean sessionBean;

    public LinkPredictionBean() {
        if (sessionBean == null) {
            sessionBean = new SessionBean();
        }
        sessionBean.setFunction("linkprediction");
        privateProperties = SingletonBean.getPrivateProperties();
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String handleFileUpload(FileUploadEvent event) {
        success = false;
        String successMsg = sessionBean.getLocaleBundle().getString("general.nouns.success");
        String is_uploaded = sessionBean.getLocaleBundle().getString("general.verb.is_uploaded");
        FacesMessage message = new FacesMessage(successMsg, event.getFile().getFileName() + " " + is_uploaded + ".");
        FacesContext.getCurrentInstance().addMessage(null, message);
        uploadedFile = event.getFile();
        try {
            uploadedFileAsByteArray = uploadedFile.getInputStream().readAllBytes();
        } catch (IOException ex) {
            System.out.println("ex:" + ex.getMessage());
        }
        showPredictions();
        return "";
    }

    public Integer showPredictions() {
        if (uploadedFile == null) {
            System.out.println("no file found for link prediction");
            return 0;
        }
        sessionBean.sendFunctionPageReport();
        HttpRequest request;
        HttpClient client = HttpClient.newHttpClient();
        Set<CompletableFuture> futures = new HashSet();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(uploadedFileAsByteArray);
        URI uri = UrlBuilder
                .empty()
                .withScheme("http")
                .withPort((Integer.valueOf(privateProperties.getProperty("nocode_api_port"))))
                .withHost("localhost")
                .withPath("api/linkprediction")
                .addParameter("nb_predictions", String.valueOf(nbPredictions))
                .toUri();
        request = HttpRequest.newBuilder()
                .POST(bodyPublisher)
                .uri(uri)
                .build();
        CompletableFuture<Void> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenAccept(resp -> {
            byte[] body = resp.body();
            String jsonResultsAsString = new String(body, StandardCharsets.UTF_8);
            try (JsonReader reader = Json.createReader(new StringReader(jsonResultsAsString))) {
                jsonObjectReturned = reader.readObject();
            } catch (JsonParsingException jsonEx) {
                System.out.println("error: the json we received from link prediction is not formatted as json");
            }
        }
        );
        futures.add(future);
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray((new CompletableFuture[0])));
        combinedFuture.join();
        if (jsonObjectReturned == null) {
            System.out.println("error: the json we received from link prediction is not formatted as json");
            return -1;
            
        }
        JsonObject predictions = jsonObjectReturned.getJsonObject("predictions");
        augmentedGexf = jsonObjectReturned.getString("gexf augmented");
        List<String> orderedListOfPredictions = predictions.keySet().stream().sorted().collect(Collectors.toList());
        topPredictions = new ArrayList();
        for (String predictionKey : orderedListOfPredictions) {
            JsonObject predictionJson = predictions.getJsonObject(predictionKey);
            Prediction prediction = new Prediction();
            prediction.setSourceId(predictionJson.getString("source node id"));
            prediction.setSourceLabel(predictionJson.getString("source node label"));
            prediction.setSourceDegree(predictionJson.getInt("source node degree"));
            prediction.setTargetId(predictionJson.getString("target node id"));
            prediction.setTargetLabel(predictionJson.getString("target node label"));
            prediction.setTargetDegree(predictionJson.getInt("target node degree"));
            prediction.setPredictionValue(predictionJson.getInt("prediction value"));
            topPredictions.add(prediction);
        }
        return 1;
    }

    public int getNbPredictions() {
        return nbPredictions;
    }

    public void setNbPredictions(int nbPredictions) {
        this.nbPredictions = nbPredictions;
    }

    public String getUploadButtonMessage() {
        return uploadButtonMessage;
    }

    public void setUploadButtonMessage(String uploadButtonMessage) {
        this.uploadButtonMessage = uploadButtonMessage;
    }

    public boolean isRenderGephiWarning() {
        return renderGephiWarning;
    }

    public void setRenderGephiWarning(boolean renderGephiWarning) {
        this.renderGephiWarning = renderGephiWarning;
    }

    public StreamedContent getFileToSave() throws FileNotFoundException {
        success = true;
        if (augmentedGexf == null) {
            System.out.println("no file found for link prediction");
            return null;
        }
        return GEXFSaver.exportGexfAsStreamedFile(augmentedGexf, "initial_graph_augmented_with_predicted_links");
    }

    public void setFileToSave(StreamedContent fileToSave) {
        this.fileToSave = fileToSave;
    }

    public List<Prediction> getTopPredictions() {
        return topPredictions;
    }

    public void setTopPredictions(List<Prediction> topPredictions) {
        this.topPredictions = topPredictions;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Prediction getSelectedLink() {
        return selectedLink;
    }

    public void setSelectedLink(Prediction selectedLink) {
        this.selectedLink = selectedLink;
    }

}
