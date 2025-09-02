package com.arplanets.spring.security.casbin.model;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

import java.util.concurrent.ConcurrentHashMap;

public class CasbinFactory {

    private static final ConcurrentHashMap<String, Model> modelMap = new ConcurrentHashMap<>();
    private static final String MODEL_CONF_TEXT = """
            [request_definition]
            r = sub,obj,act,dom
            
            [policy_definition]
            p = sub,obj,act,dom,eft
            
            [role_definition]
            g = _, _
            
            [policy_effect]
            e = some(where (p.eft == allow))
            
            [matchers]
            m = g(r.sub, p.sub) && (r.dom == p.dom) && keyMatch4(r.obj , p.obj) && keyMatch2(r.act , p.act)""";

    public static Model loadModel(String modelName) {
        return modelMap.computeIfAbsent(modelName, key -> {
            Model model = new Model();
            model.loadModelFromText(MODEL_CONF_TEXT);
            return model;
        });
    }

    public static Enforcer getEnforcer(String modelName) {
        return new Enforcer(loadModel(modelName));
    }


}
