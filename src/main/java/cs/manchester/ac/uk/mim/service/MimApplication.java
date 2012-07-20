package cs.manchester.ac.uk.mim.service;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

import cs.manchester.ac.uk.mim.service.resources.MimResource;

public class MimApplication extends Application {
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(MimResource.class);
        return s;
    }
}