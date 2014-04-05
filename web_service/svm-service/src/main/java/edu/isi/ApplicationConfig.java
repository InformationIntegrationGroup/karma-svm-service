package edu.isi;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

@ApplicationPath("/")
public class ApplicationConfig extends Application {

    public Set<Class<?>> getClasses() {
        final Set<Class<?>> resources = new HashSet<Class<?>>();

        // Add additional features such as support for Multipart.
        resources.add(MultiPartFeature.class);
        resources.add(SVM_Service.class);
        return resources;
    }
}
