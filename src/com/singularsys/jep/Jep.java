package com.singularsys.jep;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
import java.util.Map;

public class Jep {

    private static final JexlEngine ENGINE = new JexlBuilder()
            .cache(256)
            .strict(false)
            .silent(false)
            .create();

    private final Map<String, Object> variables = new HashMap<>();
    private String expression;

    public void addVariable(String name, Object value) {
        this.variables.put(name, value);
    }

    public void parse(String expression) throws JepException {
        try {
            this.expression = expression;
            // Validation immediate pour garder un comportement proche de JEP
            ENGINE.createExpression(expression);
        } catch (RuntimeException e) {
            throw new JepException("Expression invalide: " + expression, e);
        }
    }

    public Object evaluate() throws JepException {
        if (this.expression == null) {
            throw new JepException("Aucune expression n'a ete parsee");
        }
        try {
            JexlExpression compiled = ENGINE.createExpression(this.expression);
            JexlContext context = new MapContext();
            for (Map.Entry<String, Object> entry : this.variables.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
            return compiled.evaluate(context);
        } catch (RuntimeException e) {
            throw new JepException("Erreur pendant l'evaluation", e);
        }
    }
}

