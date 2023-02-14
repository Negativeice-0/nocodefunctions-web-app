/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.nocodeapp.web.front.backingbeans;

import com.github.scribejava.core.model.OAuth2AccessToken;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import net.clementlevallois.nocodeapp.web.front.http.RemoteLocal;
import net.clementlevallois.nocodeapp.web.front.http.SendReport;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 *
 * @author LEVALLOIS
 */
@Named
@SessionScoped
public class SessionBean implements Serializable {

    private String function;
    private String gazeOption = "1";
    private String userAgent;
    private ResourceBundle localeBundle;
    private boolean testServer;
    private String noRobot;

    private OAuth2AccessToken twitterOAuth2AccessToken;

    @Inject
    SingletonBean singletonBean;
    
    public SessionBean() {
    }

    @PostConstruct
    public void init() {
        final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request != null) {
            userAgent = request.getHeader("user-agent");
        } else {
            userAgent = "unknown-user-agent";
        }
        localeBundle = ResourceBundle.getBundle(SingletonBean.getPATHLOCALE(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    ;

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {

        if (function == null) {
            System.out.println("function param was null??");
            return;
        }
        if (function.contains("=") & !function.contains("?")) {
            System.out.println("weird url parameters decoded in sessionBean");
            System.out.println("url param for function is: " + function);
            this.function = function.split("=")[1];
        } else if (function.contains("=") & function.contains("?")) {
            this.function = function.split("\\?")[0];
        } else {
            this.function = function;
        }
    }

    public String getGazeOption() {
        return gazeOption;
    }

    public void setGazeOption(String gazeOption) {
        this.gazeOption = gazeOption;
    }
    
    

    public void sendFunctionPageReport() {
        SendReport send = new SendReport();
        send.initAnalytics("function launched: " + this.function, userAgent);
        send.start();
    }

    public String logout() {
        Locale currLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().getViewRoot().setLocale(currLocale);

        return "/index?faces-redirect=true";
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String host() {
        return RemoteLocal.getDomain();
    }

    public void refreshLocaleBundle() {
        localeBundle = ResourceBundle.getBundle(SingletonBean.getPATHLOCALE(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public ResourceBundle getLocaleBundle() {
        return localeBundle;
    }

    public boolean isTestServer() {
        return RemoteLocal.isTest();
    }

    public void setTestServer(boolean testServer) {
        this.testServer = testServer;
    }

    public String getNoRobot() {
        String noIndex = "<meta name=\"robots\" content=\"noindex\"/>";
        return noIndex;
    }

    public void setNoRobot(String noRobot) {
        this.noRobot = noRobot;
    }

    public OAuth2AccessToken getTwitterOAuth2AccessToken() {
        return twitterOAuth2AccessToken;
    }

    public void setTwitterOAuth2AccessToken(OAuth2AccessToken twitterOAuth2AccessToken) {
        this.twitterOAuth2AccessToken = twitterOAuth2AccessToken;
    }

    public SingletonBean getSingletonBean() {
        return singletonBean;
    }

    public void setSingletonBean(SingletonBean singletonBean) {
        this.singletonBean = singletonBean;
    }

    
    
    
    
    
    
    
    

}
