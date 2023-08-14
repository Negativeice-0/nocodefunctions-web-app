/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.nocodeapp.web.front.backingbeans;

//import com.github.scribejava.core.model.OAuth2AccessToken;
//import com.github.scribejava.core.pkce.PKCE;
//import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
//import com.twitter.clientlib.TwitterCredentialsOAuth2;
//import com.twitter.clientlib.auth.TwitterOAuth20Service;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import jakarta.enterprise.context.ApplicationScoped;
import org.omnifaces.cdi.Startup;

/**
 *
 * @author LEVALLOIS
 */
@Startup
@ApplicationScoped
public class SingletonBean {

    private final static String PATHLOCALE = "net.clementlevallois.nocodeapp.web.front.resources.i18n.text";
    private static Properties privateProperties;
    private final static String PATHLOCALDEV = "C:\\Users\\levallois\\open\\nocode-app-web-front\\";
    private final static String PATHREMOTEDEV = "/home/waouh/nocodeapp-web/";
    private static String rootProps;

    public SingletonBean() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                rootProps = PATHLOCALDEV;
            } else {
                rootProps = PATHREMOTEDEV;
            }
            InputStream is = new FileInputStream(rootProps + "private/private.properties");
            privateProperties = new Properties();
            privateProperties.load(is);


        } catch (UnknownHostException ex) {
            System.out.println("ex:" + ex.getMessage());
        } catch (FileNotFoundException ex) {
            System.out.println("ex:" + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("ex:" + ex.getMessage());
        }

    }

    public static Properties getPrivateProperties() {
        return privateProperties;
    }

    public static String getPATHLOCALE() {
        return PATHLOCALE;
    }
}
